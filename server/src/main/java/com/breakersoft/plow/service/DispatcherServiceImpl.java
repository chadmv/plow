package com.breakersoft.plow.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Folder;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dao.DispatchDao;
import com.breakersoft.plow.dao.ProcDao;
import com.breakersoft.plow.dispatcher.DispatchFolder;
import com.breakersoft.plow.dispatcher.DispatchJob;
import com.breakersoft.plow.dispatcher.DispatchLayer;
import com.breakersoft.plow.dispatcher.DispatchNode;
import com.breakersoft.plow.dispatcher.DispatchProc;
import com.breakersoft.plow.dispatcher.DispatchProject;
import com.breakersoft.plow.dispatcher.DispatchTask;

@Service
@Transactional
public class DispatcherServiceImpl implements DispatcherService {

    @Autowired
    private DispatchDao dispatchDao;

    @Autowired
    private ProcDao procDao;

    @Override
    @Transactional(readOnly=true)
    public List<DispatchProject> getSortedProjectList(DispatchNode node) {
        return dispatchDao.getSortedProjectList(node);
    }

    @Override
    @Transactional(readOnly=true)
    public DispatchJob getDispatchJob(Job job) {
        return dispatchDao.getDispatchJob(job);
    }

    @Override
    @Transactional(readOnly=true)
    public DispatchFolder getDispatchFolder(Folder folder) {
        return dispatchDao.getDispatchFolder(folder);
    }

    @Override
    @Transactional(readOnly=true)
    public DispatchNode getDispatchNode(String name) {
        return dispatchDao.getDispatchNode(name);
    }

    @Override
    @Transactional(readOnly=true)
    public List<DispatchTask> getTasks(DispatchLayer layer, DispatchNode node) {
        return dispatchDao.getFrames(layer, node);
    }

    @Override
    @Transactional(readOnly=true)
    public List<DispatchLayer> getDispatchLayers(Job job) {
        return dispatchDao.getDispatchLayers(job);
    }

    @Override
    public void createDispatchProc (DispatchProc proc) {
        procDao.create(proc);
    }

    @Override
    public boolean reserveTask(Task task) {
        return dispatchDao.reserveTask(task);
    }

    @Override
    public boolean unreserveTask(Task task) {
        return dispatchDao.unreserveTask(task);
    }



}
