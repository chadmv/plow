package com.breakersoft.plow.test.service;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;

import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.service.StateManager;
import com.breakersoft.plow.test.AbstractTest;
import com.google.common.eventbus.Subscribe;

public class StateManagerTests extends AbstractTest {

    @Resource
    StateManager stateManager;

    @Resource
    EventManager eventManager;

    private boolean jobLaunchEventHandled;
    private boolean jobShutdownEventHandled;

    @Before
    public void reset() {
        eventManager.register(this);
        jobLaunchEventHandled = false;
        jobShutdownEventHandled = false;
    }

    @Test
    public void testLaunchJob() {
        JobLaunchEvent event = jobService.launch(getTestJobSpec());
        assertLayerCount(event.getJob(), 1);
        assertFrameCount(event.getJob(), 10);
        assertTrue(jobLaunchEventHandled);
    }

    @Test
    public void testShutdownJob() {
        JobLaunchEvent event = jobService.launch(getTestJobSpec());
        stateManager.shutdownJob(event.getJob());
        assertTrue(jobShutdownEventHandled);
    }

    @Test
    public void testKilJobl() {
        JobLaunchEvent event = jobService.launch(getTestJobSpec());
        stateManager.killJob(event.getJob(), "unit test kill");
        assertTrue(jobShutdownEventHandled);
    }

    @Subscribe
    public void handleJobLaunchEvent(JobLaunchEvent event) {
        jobLaunchEventHandled = true;
    }

    @Subscribe
    public void handleJobShutdownEvent(JobLaunchEvent event) {
        jobShutdownEventHandled = true;
    }
}
