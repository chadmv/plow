package com.breakersoft.plow.test.dispatcher;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;

import com.breakersoft.plow.dispatcher.DispatchService;
import com.breakersoft.plow.dispatcher.FrontEndDispatcher;
import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.event.EventManagerImpl;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.service.JobLauncherService;
import com.breakersoft.plow.test.AbstractTest;

public class FrontEndDispatcherTests extends AbstractTest {

    @Resource
    FrontEndDispatcher dispatcher;

    @Resource
    DispatchService dispatcherService;

    @Resource
    JobLauncherService jobLaucherService;

    @Resource
    EventManager eventManager;

    @Before
    public void init() {
        // Disable the event manager so we can add the job
        // to the dispatch thread on its own.
        ((EventManagerImpl) eventManager).setEnabled(false);
    }

    @Test
    public void testAddAndRemoveJob() {

        JobLaunchEvent event = jobLaucherService.launch(getTestBlueprint());
        DispatchJob job = dispatcherService.getDispatchJob(event);

        dispatcher.addJob(job);
        assertEquals(1, dispatcher.getTotalJobs());

        dispatcher.removeJob(job);
        assertEquals(0, dispatcher.getTotalJobs());
    }

    @Test
    public void testGetJob() {
        JobLaunchEvent event = jobLaucherService.launch(getTestBlueprint());
        DispatchJob job = dispatcherService.getDispatchJob(event);

        dispatcher.addJob(job);
        assertEquals(job, dispatcher.getJob(job.getJobId()));
        dispatcher.removeJob(job);
    }

    @Test
    public void testHandleJobLaunchEvent() {
        JobLaunchEvent event = jobLaucherService.launch(getTestBlueprint());
        DispatchJob job = dispatcherService.getDispatchJob(event);

        dispatcher.handleJobLaunchEvent(event);
        assertEquals(1, dispatcher.getTotalJobs());
    }

}
