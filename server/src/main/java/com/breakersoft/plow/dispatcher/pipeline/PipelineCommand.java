package com.breakersoft.plow.dispatcher.pipeline;

import java.util.UUID;

import com.breakersoft.plow.JobId;

public abstract class PipelineCommand implements JobId {

    private final UUID jobId;

    public PipelineCommand (UUID jobId) {
        this.jobId = jobId;
    }

    @Override
    public UUID getJobId() {
        return jobId;
    }

    public abstract void process();

}
