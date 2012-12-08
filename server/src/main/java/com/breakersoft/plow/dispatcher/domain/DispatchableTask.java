package com.breakersoft.plow.dispatcher.domain;

import java.util.UUID;

import com.breakersoft.plow.Task;

public class DispatchableTask implements Task {

    public UUID taskId;
    public UUID layerId;
    public UUID jobId;
    public int minCores;
    public int minRam;

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
}
