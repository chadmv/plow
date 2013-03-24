package com.breakersoft.plow.service;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.Folder;
import com.breakersoft.plow.Project;

public interface ProjectService {

    Project createProject(String name, String title);

    Project getProject(UUID id);

    Folder createFolder(Project project, String name);

    List<Project> getProjects();

    void setProjectActive(Project project, boolean value);
}
