package com.breakersoft.plow.service;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.thrift.Blueprint;

public interface JobLauncherService {

    JobLaunchEvent launch(Blueprint bp);
    void shutdown(Job job);
}
