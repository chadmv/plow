package com.breakersoft.plow.service;

import java.util.List;

import com.breakersoft.plow.Folder;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.dispatcher.DispatchFolder;
import com.breakersoft.plow.dispatcher.DispatchJob;
import com.breakersoft.plow.dispatcher.DispatchLayer;
import com.breakersoft.plow.dispatcher.DispatchNode;
import com.breakersoft.plow.dispatcher.DispatchProc;
import com.breakersoft.plow.dispatcher.DispatchProject;
import com.breakersoft.plow.dispatcher.DispatchTask;

public interface DispatcherService {

    List<DispatchProject> getSortedProjectList(DispatchNode node);

    DispatchJob getDispatchJob(Job job);

    DispatchFolder getDispatchFolder(Folder folder);

    DispatchNode getDispatchNode(String name);

    List<DispatchTask> getTasks(DispatchLayer layer, DispatchNode node);

    void createDispatchProc(DispatchProc proc);

    List<DispatchLayer> getDispatchLayers(Job job);
}
