package com.breakersoft.plow.service;

import java.util.List;

import com.breakersoft.plow.Folder;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.dispatcher.DispatchFolder;
import com.breakersoft.plow.dispatcher.DispatchJob;
import com.breakersoft.plow.dispatcher.DispatchNode;

public interface DispatcherService {

    List<Project> getSortedProjectList(DispatchNode node);

    DispatchJob getDispatchJob(Job job);

    DispatchFolder getDispatchFolder(Folder folder);
}
