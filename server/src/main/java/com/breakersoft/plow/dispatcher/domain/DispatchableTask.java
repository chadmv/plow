package com.breakersoft.plow.dispatcher.domain;

import java.util.UUID;

import com.breakersoft.plow.Project;
import com.breakersoft.plow.Task;

public class DispatchableTask implements Task, Project {

    public UUID taskId;
    public UUID layerId;
    public UUID jobId;
    public UUID projectId;

    public int minCores;
    public int minRam;
    public String name;

    public UUID getProjectId() {
        return projectId;
    }

    @Override
    public UUID getJobId() {
        return jobId;
    }

    @Override
    public UUID getLayerId() {
        return layerId;
    }

    @Override
    public UUID getTaskId() {
        return taskId;
    }

    @Override
    public String getName() {
        return name;
    }
}
