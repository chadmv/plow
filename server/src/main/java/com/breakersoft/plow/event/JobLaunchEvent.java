package com.breakersoft.plow.event;

import com.breakersoft.plow.Folder;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.thrift.JobBp;

public class JobLaunchEvent implements Event {

    private final Job job;
    private final Folder folder;
    private final JobBp blueprint;


    public JobLaunchEvent(Job job, Folder folder, JobBp blueprint) {
        this.job = job;
        this.folder = folder;
        this.blueprint = blueprint;
    }

    public Job getJob() {
        return job;
    }

    public JobBp getBlueprint() {
        return blueprint;
    }

    public Folder getFolder() {
        return folder;
    }
}
