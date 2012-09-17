package com.breakersoft.plow.service;

import java.util.List;

import com.breakersoft.plow.Project;
import com.breakersoft.plow.dispatcher.DispatchNode;

public interface DispatcherService {

    List<Project> getSortedProjectList(DispatchNode node);


}
