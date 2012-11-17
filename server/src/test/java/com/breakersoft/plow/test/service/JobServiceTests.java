package com.breakersoft.plow.test.service;

import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;

import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.event.EventManagerImpl;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.test.AbstractTest;
import com.google.common.eventbus.Subscribe;

public class JobServiceTests extends AbstractTest {

    @Resource
    EventManager eventManager;

    private boolean jobLaunchEventHandled;

    @Before
    public void reset() {
        ((EventManagerImpl) eventManager).setEnabled(true);
        eventManager.register(this);
        jobLaunchEventHandled = false;
    }

    @Test
    public void testLaunchJob() {
        JobLaunchEvent event = jobService.launch(getTestJobSpec());
        assertLayerCount(event.getJob(), 1);
        assertTaskCount(event.getJob(), 10);
        assertTrue(jobLaunchEventHandled);
    }

    @Test
    public void testLaunchJobWithManualTasks() {
        eventManager.register(this);
        JobLaunchEvent event =
                jobService.launch(getTestJobSpecManualTasks("manual_test"));
        assertLayerCount(event.getJob(), 1);
        assertTaskCount(event.getJob(), 1);
        assertTrue(jobLaunchEventHandled);
    }

    @Subscribe
    public void handleJobLaunchEvent(JobLaunchEvent event) {
        jobLaunchEventHandled = true;
    }
}
