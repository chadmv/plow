package com.breakersoft.plow.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Project;
import com.breakersoft.plow.dao.DispatchDao;
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
}
