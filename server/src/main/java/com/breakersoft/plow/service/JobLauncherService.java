package com.breakersoft.plow.service;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.thrift.JobBp;

public interface JobLauncherService {

    JobLaunchEvent launch(JobBp bp);
    void shutdown(Job job);
}
