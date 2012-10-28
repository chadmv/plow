package com.breakersoft.plow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.breakersoft.plow.Job;

@Component
public class JobStateManager {

    @Autowired
    JobService jobService;

    @Autowired
    RndProcessManager processManager;

    public boolean killJob(Job job, String reason) {
        final boolean killResult = jobService.shutdown(job);
        if (killResult) {
            processManager.killProcs(job, reason);
        }
        return killResult;
    }
}
