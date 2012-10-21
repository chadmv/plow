package com.breakersoft.plow.dispatcher;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.Proc;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dao.DispatchDao;
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
import com.breakersoft.plow.event.JobBookedEvent;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.event.JobUnbookedEvent;

@Service
@Transactional
public class DispatchServiceImpl implements DispatchService {

    @Autowired
    private DispatchDao dispatchDao;

    @Autowired
    private ProcDao procDao;

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
    public void createDispatchProc (DispatchProc proc) {
        procDao.create(proc);
    }

    @Override
    public boolean removeProc(Proc proc) {
        return procDao.delete(proc);
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
    public DispatchProc allocateDispatchProc(DispatchNode node, DispatchTask task) {

        node.decrement(task.getMinCores(), task.getMinMemory());

        DispatchProc proc = new DispatchProc();
        proc.setTaskId(task.getTaskId());
        proc.setNodeId(node.getNodeId());
        proc.setQuotaId(quotaDao.getQuota(node, task).getQuotaId());
        proc.setCores(task.getMinCores());
        proc.setTaskName(task.getName());
        proc.setNodeName(node.getName());
        proc.setJobId(task.getJobId());
        proc.setLayerId(task.getLayerId());
        proc.setTags(task.getTags());
        createDispatchProc(proc);
        return proc;
    }

    @Override
    public void unbookProc(DispatchProc proc) {
        if (proc == null) {
            return;
        }
        proc.setTaskId(null);
        removeProc(proc);
        eventManager.post(new JobUnbookedEvent(proc));
    }
}
