package com.breakersoft.plow.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Folder;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.dao.DispatchDao;
import com.breakersoft.plow.dispatcher.DispatchFolder;
import com.breakersoft.plow.dispatcher.DispatchJob;
import com.breakersoft.plow.dispatcher.DispatchNode;
import com.breakersoft.plow.dispatcher.DispatchProject;

@Service
@Transactional
public class DispatcherServiceImpl implements DispatcherService {

    @Autowired
    private DispatchDao dispatchDao;

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
}
