package com.breakersoft.plow.test.dispatcher;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;

import com.breakersoft.plow.Node;
import com.breakersoft.plow.dispatcher.BookingThread;
import com.breakersoft.plow.dispatcher.DispatchConfiguration;
import com.breakersoft.plow.dispatcher.DispatchService;
import com.breakersoft.plow.dispatcher.FrontEndDispatcher;
import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.event.EventManagerImpl;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.service.JobLauncherService;
import com.breakersoft.plow.service.NodeService;
import com.breakersoft.plow.test.AbstractTest;

public class BookingThreadTests extends AbstractTest {

    @Resource
    FrontEndDispatcher dispatcher;

    @Resource
    DispatchService dispatcherService;

    @Resource
    JobLauncherService jobLaucherService;

    @Resource
    NodeService nodeService;

    @Resource
    EventManager eventManager;

    @Resource
    DispatchConfiguration dispatchConfig;

    @Before
    public void init() {
        // Disable the event manager so we can add the job
        // to the dispatch thread on its own.
        ((EventManagerImpl) eventManager).setEnabled(false);
    }

    @Test
    public void testHandleJobLaunchedEvent() throws InterruptedException {

        JobLaunchEvent event = jobLaucherService.launch(getTestBlueprint());
        DispatchJob job = dispatcherService.getDispatchJob(event);

        BookingThread thread = dispatchConfig.getBookingThread();
        assertEquals(0, thread.getAddedJobCount());

        thread.addJob(job);
        assertEquals(1, thread.getAddedJobCount());

        thread.update();
        assertEquals(0, thread.getAddedJobCount());
        assertEquals(1, thread.getTotalJobCount());

        thread.removeJob(job);
        assertEquals(1, thread.getRemovedJobCount());
        thread.update();
        assertEquals(0, thread.getTotalJobCount());
    }

    @Test
    public void testDispatchNode_NoJobs() throws InterruptedException {
        BookingThread thread = dispatchConfig.getBookingThread();

        Node node = nodeService.createNode(getTestNodePing());
        DispatchNode dnode = dispatcherService.getDispatchNode(node.getName());
        thread.book(dnode);
    }

    @Test
    public void testDispatchNode_Jobs() throws InterruptedException {

        BookingThread thread = dispatchConfig.getBookingThread();
        JobLaunchEvent event = jobLaucherService.launch(getTestBlueprint());
        DispatchJob job = dispatcherService.getDispatchJob(event);

        Node node = nodeService.createNode(getTestNodePing());
        DispatchNode dnode = dispatcherService.getDispatchNode(node.getName());

        thread.addJob(job);
        thread.update();
        thread.book(dnode);
    }
}
