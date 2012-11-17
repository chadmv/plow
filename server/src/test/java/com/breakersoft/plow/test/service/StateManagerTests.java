package com.breakersoft.plow.test.service;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;

import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.event.EventManagerImpl;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.service.StateManager;
import com.breakersoft.plow.test.AbstractTest;
import com.google.common.eventbus.Subscribe;

public class StateManagerTests extends AbstractTest {

    @Resource
    StateManager stateManager;

    @Resource
    EventManager eventManager;

    private boolean jobShutdownEventHandled;

    @Before
    public void reset() {
        ((EventManagerImpl) eventManager).setEnabled(true);
        eventManager.register(this);
        jobShutdownEventHandled = false;
    }

    @Test
    public void testShutdownJob() {
        JobLaunchEvent event = jobService.launch(getTestJobSpec());
        stateManager.shutdownJob(event.getJob());
        assertTrue(jobShutdownEventHandled);
    }

    @Test
    public void testKillJob() {
        JobLaunchEvent event = jobService.launch(getTestJobSpec());
        stateManager.killJob(event.getJob(), "unit test kill");
        assertTrue(jobShutdownEventHandled);
    }
    @Subscribe
    public void handleJobShutdownEvent(JobLaunchEvent event) {
        jobShutdownEventHandled = true;
    }
}
