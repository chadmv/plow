package com.breakersoft.plow.dispatcher;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.JobId;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dao.ProcDao;
import com.breakersoft.plow.dao.QuotaDao;
import com.breakersoft.plow.dispatcher.dao.DispatchTaskDao;
import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchProject;
import com.breakersoft.plow.dispatcher.domain.DispatchResource;
import com.breakersoft.plow.dispatcher.domain.DispatchStats;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;
import com.breakersoft.plow.event.EventManager;
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
    private DispatchTaskDao dispatchTaskDao;

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
        return dispatchTaskDao.reserve(task);
    }

    @Override
    public boolean unreserveTask(Task task) {
        return dispatchTaskDao.unreserve(task);
    }

    @Override
    @Transactional(readOnly=true)
    public List<DispatchProc> getOrphanProcs() {
        return dispatchDao.getOrphanProcs();
    }

    @Override
    public boolean startTask(String hostname, DispatchTask task) {
        if (dispatchTaskDao.start(task)) {
            logger.info("Started {}", task);

            DispatchStats.taskStartedCount.incrementAndGet();
            return true;
        }
        DispatchStats.taskStartedFailureCount.incrementAndGet();
        return false;
    }

    @Override
    public boolean stopTask(Task task, TaskState state) {
        if (dispatchTaskDao.stop(task, state)) {
            logger.info("Stopping {}, new state: {}", task, state.toString());

            DispatchStats.taskStoppedCount.incrementAndGet();
            return true;
        }
        DispatchStats.taskStoppedFailureCount.incrementAndGet();
        return false;
    }

    @Override
    public void unassignProc(DispatchProc proc) {
        procDao.unassign(proc);
        proc.setTaskId(null);
    }

    @Override
    public void assignProc(DispatchProc proc, DispatchTask task) {
        procDao.assign(proc, task);
        proc.setTaskId(task.getTaskId());
    }

    @Override
    public DispatchProc allocateProc(DispatchNode node, DispatchTask task) {

        logger.info("Allocating proc on {}", node);

        // Fast quota check
        if(!quotaDao.check(node, task, task.minCores)) {
              throw new PlowDispatcherException(
                      "Failed to allocatae a proc from " + node.getName() + ", failed quota check.");
        }

        try {
            return procDao.create(node, task);

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

        if (!procDao.delete(proc)) {
            logger.warn("{} was alredy deallocated.", proc);
        }
    }

    @Override
    public List<DispatchTask> getDispatchableTasks(JobId job,
            DispatchResource resource) {
        return dispatchTaskDao.getDispatchableTasks(job, resource);
    }

    @Override
    public RunTaskCommand getRuntaskCommand(Task task) {
        return dispatchTaskDao.getRunTaskCommand(task);
    }
}
