package com.breakersoft.plow.event;

import com.breakersoft.plow.FilterableJob;
import com.breakersoft.plow.Folder;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.thrift.JobSpecT;

public class JobLaunchEvent implements Event {

    private final FilterableJob job;
    private final Folder folder;
    private final JobSpecT jobSpec;


    public JobLaunchEvent(FilterableJob job, Folder folder, JobSpecT jobSpec) {
        this.job = job;
        this.folder = folder;
        this.jobSpec = jobSpec;
    }

    public FilterableJob getJob() {
        return job;
    }

    public JobSpecT getJobSpec() {
        return jobSpec;
    }

    public Folder getFolder() {
        return folder;
    }
}
