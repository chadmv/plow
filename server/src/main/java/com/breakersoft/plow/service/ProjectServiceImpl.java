package com.breakersoft.plow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Project;
import com.breakersoft.plow.dao.FolderDao;
import com.breakersoft.plow.dao.ProjectDao;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    ProjectDao projectDao;

    @Autowired
    FolderDao folderDao;

    public Project createProject(String name, String title) {
        Project project = projectDao.create(name, title);
        return project;
    }

}
