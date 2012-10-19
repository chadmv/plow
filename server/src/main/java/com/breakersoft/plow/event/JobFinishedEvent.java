package com.breakersoft.plow.event;

import com.breakersoft.plow.Job;

public class JobFinishedEvent {

    private final Job job;

    public JobFinishedEvent(Job job) {
        this.job = job;
    }

    public Job getJob() {
        return job;
    }
}
