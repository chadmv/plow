package com.breakersoft.plow.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Defaults;
import com.breakersoft.plow.Folder;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.dao.FolderDao;
import com.breakersoft.plow.dao.ProjectDao;
import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.event.FolderCreatedEvent;
import com.breakersoft.plow.event.ProjectCreatedEvent;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    EventManager eventManager;

    @Autowired
    ProjectDao projectDao;

    @Autowired
    FolderDao folderDao;

    @Override
    public Project createProject(String title, String code) {
        Project project = projectDao.create(title, code);
        Folder folder = createFolder(project, Defaults.FOLDER_DEFAULT_NAME);
        projectDao.setDefaultFolder(project, folder);
        eventManager.post(new ProjectCreatedEvent(project));
        return project;
    }

    @Override
    public Folder createFolder(Project project, String name) {
        Folder folder = folderDao.createFolder(project, name);
        eventManager.post(new FolderCreatedEvent(folder));
        return folder;
    }

    @Override
    public Project getProject(UUID id) {
        return projectDao.get(id);
    }

    @Override
    public List<Project> getProjects() {
        return projectDao.getAll();
    }

	@Override
	public void setProjectActive(Project project, boolean value) {
		projectDao.setActive(project, value);
	}

	@Override
	public void setFolderMaxCores(Folder folder, int value) {
		folderDao.setMaxCores(folder, value);
	}

	@Override
	public void setFolderMinCores(Folder folder, int value) {
		folderDao.setMinCores(folder, value);
	}

	@Override
	public void setFolderName(Folder folder, String name) {
		folderDao.setName(folder, name);
	}

	@Override
	public void deleteFolder(Folder folder) {
		folderDao.delete(folder);
	}
}
