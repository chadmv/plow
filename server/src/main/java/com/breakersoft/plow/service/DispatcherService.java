package com.breakersoft.plow.service;

import java.util.List;

import com.breakersoft.plow.Folder;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.dispatcher.DispatchFolder;
import com.breakersoft.plow.dispatcher.DispatchJob;
import com.breakersoft.plow.dispatcher.DispatchNode;
import com.breakersoft.plow.dispatcher.DispatchProject;

public interface DispatcherService {

    List<DispatchProject> getSortedProjectList(DispatchNode node);

    DispatchJob getDispatchJob(Job job);

    DispatchFolder getDispatchFolder(Folder folder);

    DispatchNode getDispatchNode(String name);
}
