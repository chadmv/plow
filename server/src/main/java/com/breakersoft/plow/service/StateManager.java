package com.breakersoft.plow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.breakersoft.plow.Depend;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.event.JobFinishedEvent;

@Component
public class StateManager {

    @Autowired
    JobService jobService;

    @Autowired
    DependService dependService;

    @Autowired
    EventManager eventManager;

    @Autowired
    RndProcessManager processManager;

    public boolean killJob(Job job, String reason) {
        final boolean killResult = shutdownJob(job);
        if (killResult) {
            processManager.killProcs(job, reason);
        }
        return killResult;
    }

    public boolean shutdownJob(Job job) {
        if (jobService.shutdown(job)) {
            satisfyDependsOn(job);
            eventManager.post(new JobFinishedEvent(job));
            return true;
        }
        return false;
    }

    public void satisfyDependsOn(Job job) {
        for (Depend depend: dependService.getOnJobDepends(job)) {
            dependService.satisfyDepend(depend);
        }
    }

    public void satisfyDependsOn(Task task) {
        for (Depend depend: dependService.getOnTaskDepends(task)) {
            dependService.satisfyDepend(depend);
        }
    }

    public void satisfyDependsOn(Layer layer) {
        for (Depend depend: dependService.getOnLayerDepends(layer)) {
            dependService.satisfyDepend(depend);
        }
    }
}
