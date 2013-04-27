package com.breakersoft.plow.dispatcher;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.JobId;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.Quota;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dao.NodeDao;
import com.breakersoft.plow.dao.ProcDao;
import com.breakersoft.plow.dao.QuotaDao;
import com.breakersoft.plow.dao.TaskDao;
import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchProject;
import com.breakersoft.plow.dispatcher.domain.DispatchResource;
import com.breakersoft.plow.dispatcher.domain.DispatchStats;
import com.breakersoft.plow.dispatcher.domain.DispatchableTask;
import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.event.ProcDeallocatedEvent;
import com.breakersoft.plow.exceptions.PlowDispatcherException;
import com.breakersoft.plow.rnd.thrift.RunTaskCommand;
import com.breakersoft.plow.thrift.TaskState;

@Service
@Transactional
public class DispatchServiceImpl implements DispatchService {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(DispatchServiceImpl.class);

    @Autowired
    private DispatchDao dispatchDao;

    @Autowired
    private ProcDao procDao;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private TaskDao taskDao;

    @Autowired
    private QuotaDao quotaDao;

    @Autowired
    EventManager eventManager;

    @Override
    @Transactional(readOnly=true)

    public List<DispatchJob> getDispatchJobs(DispatchProject project, DispatchNode node) {
        return dispatchDao.getDispatchJobs(project, node);
    }

    public DispatchJob getDispatchJob(UUID id) {
        return dispatchDao.getDispatchJob(id);
    }

    @Override
    @Transactional(readOnly=true)
    public List<DispatchProject> getSortedProjectList(DispatchNode node) {
        return dispatchDao.getSortedProjectList(node);
    }

    @Override
    @Transactional(readOnly=true)
    public DispatchProc getDispatchProc(String id) {
        return dispatchDao.getDispatchProc(UUID.fromString(id));
    }

    @Override
    @Transactional(readOnly=true)
    public DispatchNode getDispatchNode(String name) {
        return dispatchDao.getDispatchNode(name);
    }

    @Override
    public boolean reserveTask(Task task) {
        return taskDao.reserve(task);
    }

    @Override
    public boolean unreserveTask(Task task) {
        return taskDao.unreserve(task);
    }

    @Override
    public boolean startTask(String hostname, DispatchableTask task) {
        if (taskDao.start(task, task.minCores, task.minRam)) {
            taskDao.resetTaskDispatchData(task, hostname);
            DispatchStats.taskStartedCount.incrementAndGet();
            return true;
        }
        DispatchStats.taskStartedFailureCount.incrementAndGet();
        return false;
    }

    @Override
    public boolean stopTask(Task task, TaskState state) {
        if (taskDao.stop(task, state)) {
            taskDao.clearLastLogLine(task);
            DispatchStats.taskStoppedCount.incrementAndGet();
            return true;
        }
        DispatchStats.taskStoppedFailureCount.incrementAndGet();
        return false;
    }

    @Override
    public void unassignProc(DispatchProc proc) {
        procDao.update(proc, null);
        proc.setTaskId(null);
    }

    @Override
    public void assignProc(DispatchProc proc, DispatchableTask task) {
        procDao.update(proc, task);
        proc.setTaskId(task.getTaskId());
    }

    @Override
    public DispatchProc allocateProc(DispatchNode node, DispatchableTask task) {

        logger.info("Allocating proc on {}", node);

        // Fast quota check
        if(!quotaDao.check(node, task, task.minCores)) {
              throw new PlowDispatcherException(
                      "Failed to allocatae a proc from " + node.getName() + ", failed quota check.");
        }

        try {
            nodeDao.allocate(node, task.minCores, task.minRam);
            quotaDao.allocate(node, task, task.minCores);
            DispatchProc proc = procDao.create(node, task);
            dispatchDao.incrementDispatchTotals(proc);
            return proc;

        } catch (Exception e) {
            throw new PlowDispatcherException(
                    "Failed to allocatae a proc from " + node.getName() + "," + e, e);
        }

    }

    @Override
    public void deallocateProc(DispatchProc proc, String why) {

        if (proc == null) {
            return;
        }

        logger.info("deallocating {}, {}", proc, why);

        final Quota quota = quotaDao.getQuota(proc);
        final Node node = nodeDao.get(proc.getNodeId());

        // Updates tables
        // proc
        // quota
        // node
        // folder_dsp
        // job_dsp
        // layer_dsp

        if (procDao.delete(proc)) {
            quotaDao.free(quota, proc.getIdleCores());
            nodeDao.freeResources(node, proc.getIdleCores(), proc.getIdleRam());
            dispatchDao.decrementDispatchTotals(proc);
        }
        else {
            logger.warn("{} was alredy deallocated.", proc.getProcId());
        }

        // TODO: most to post transaction event.
        eventManager.post(new ProcDeallocatedEvent(proc));
    }

    @Override
    public List<DispatchableTask> getDispatchableTasks(JobId job,
            DispatchResource resource) {
        return dispatchDao.getDispatchableTasks(job, resource);
    }

    @Override
    public RunTaskCommand getRuntaskCommand(Task task) {
        return dispatchDao.getRunTaskCommand(task);
    }
}
