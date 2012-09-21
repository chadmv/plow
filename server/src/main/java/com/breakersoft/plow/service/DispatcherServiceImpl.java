package com.breakersoft.plow.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Folder;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.dao.DispatchDao;
import com.breakersoft.plow.dispatcher.DispatchFolder;
import com.breakersoft.plow.dispatcher.DispatchJob;
import com.breakersoft.plow.dispatcher.DispatchNode;

@Service
@Transactional
public class DispatcherServiceImpl implements DispatcherService {

    private DispatchDao dispatchDao;

    @Override
    public List<Project> getSortedProjectList(DispatchNode node) {
        // TODO Auto-generated method stub
        return null;
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
}
