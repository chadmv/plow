package com.breakersoft.plow.event;

import com.breakersoft.plow.Folder;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.thrift.JobSpecT;

public class JobLaunchEvent implements Event {

    private final Job job;
    private final Folder folder;
    private final JobSpecT jobSpec;


    public JobLaunchEvent(Job job, Folder folder, JobSpecT jobSpec) {
        this.job = job;
        this.folder = folder;
        this.jobSpec = jobSpec;
    }

    public Job getJob() {
        return job;
    }

    public JobSpecT getJobSpec() {
        return jobSpec;
    }

    public Folder getFolder() {
        return folder;
    }
}
