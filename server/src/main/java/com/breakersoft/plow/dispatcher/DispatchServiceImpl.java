package com.breakersoft.plow.dispatcher;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.Quota;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dao.DispatchDao;
import com.breakersoft.plow.dao.NodeDao;
import com.breakersoft.plow.dao.ProcDao;
import com.breakersoft.plow.dao.QuotaDao;
import com.breakersoft.plow.dao.TaskDao;
import com.breakersoft.plow.dispatcher.domain.DispatchFolder;
import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchLayer;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchProject;
import com.breakersoft.plow.dispatcher.domain.DispatchResource;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;
import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.event.JobUnbookedEvent;
import com.breakersoft.plow.rnd.thrift.RunTaskCommand;

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
    public List<DispatchProject> getSortedProjectList(DispatchNode node) {
        return dispatchDao.getSortedProjectList(node);
    }

    @Override
    @Transactional(readOnly=true)
    public DispatchJob getDispatchJob(JobLaunchEvent event) {
        DispatchJob djob = dispatchDao.getDispatchJob(event.getJob());
        return djob;
    }

    @Override
    @Transactional(readOnly=true)
    public List<DispatchJob> getDispatchJobs() {
        return dispatchDao.getDispatchJobs();
    }

    @Override
    @Transactional(readOnly=true)
    public DispatchProc getDispatchProc(String id) {
        return dispatchDao.getDispatchProc(UUID.fromString(id));
    }

    @Override
    @Transactional(readOnly=true)
    public DispatchFolder getDispatchFolder(UUID folder) {
        return dispatchDao.getDispatchFolder(folder);
    }

    @Override
    @Transactional(readOnly=true)
    public DispatchNode getDispatchNode(String name) {
        return dispatchDao.getDispatchNode(name);
    }

    @Override
    @Transactional(readOnly=true)
    public List<DispatchTask> getDispatchTasks(DispatchLayer layer, DispatchResource resource) {
        return dispatchDao.getDispatchTasks(layer, resource);
    }

    @Override
    @Transactional(readOnly=true)
    public List<DispatchLayer> getDispatchLayers(Job job, DispatchResource resource) {
        return dispatchDao.getDispatchLayers(job, resource);
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
    public void unassignProc(DispatchProc proc) {
        procDao.update(proc, null);
        proc.setTaskId(null);
    }

    @Override
    public void assignProc(DispatchProc proc, DispatchTask task) {
        procDao.update(proc, task);
        proc.setTaskId(task.getTaskId());
    }

    @Override
    public DispatchProc createProc(DispatchNode node, DispatchTask task) {

        final Quota quota = quotaDao.getQuota(node, task);

        final DispatchProc proc = new DispatchProc();
        proc.setTaskId(task.getTaskId());
        proc.setNodeId(node.getNodeId());
        proc.setQuotaId(quota.getQuotaId());
        proc.setCores(task.getMinCores());
        proc.setTaskName(task.getName());
        proc.setHostname(node.getName());
        proc.setJobId(task.getJobId());
        proc.setLayerId(task.getLayerId());
        proc.setTags(task.getTags());

        procDao.create(proc);
        nodeDao.allocateResources(node, task.getMinCores(), task.getMinMemory());
        quotaDao.allocateResources(quota, task.getMinCores());

        node.decrement(task.getMinCores(), task.getMinMemory());
        return proc;
    }

    @Override
    public RunTaskCommand getRuntaskCommand(DispatchTask task, DispatchProc proc) {
        return dispatchDao.getRunTaskCommand(task, proc);
    }

    @Override
    public void unbookProc(DispatchProc proc, String why) {

        if (proc == null) {
            logger.info("proc is null;");
            return;
        }

        if (!proc.isAllocated()) {
            logger.warn("Ignoring unbook proc on {}, {}", proc.getProcId(), why);
            return;
        }

        logger.info("unbooking proc: {}, {}", proc.getProcId(), why);

        if (procDao.delete(proc)) {

            logger.info("Proc unbooked {}", proc.getProcId());

            final Quota quota = quotaDao.get(proc.getQuotaId());
            final Node node = nodeDao.get(proc.getNodeId());

            nodeDao.freeResources(node, proc.getCores(), proc.getMemory());
            quotaDao.freeResources(quota, proc.getCores());

            proc.setAllocated(false);
            eventManager.post(new JobUnbookedEvent(proc));
        }
        else {
            logger.warn("{} was alredy unbooked.", proc.getProcId());
        }
    }
}
