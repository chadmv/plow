package com.breakersoft.plow.event;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.json.Blueprint;

public class JobLaunchEvent implements Event {

    private final Job job;
    private final Blueprint blueprint;

    public JobLaunchEvent(Job job, Blueprint blueprint) {
        this.job = job;
        this.blueprint = blueprint;
    }

    public Job getJob() {
        return job;
    }

    public Blueprint getBlueprint() {
        return blueprint;
    }
}
