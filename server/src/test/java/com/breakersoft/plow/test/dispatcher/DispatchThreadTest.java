package com.breakersoft.plow.test.dispatcher;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;

import com.breakersoft.plow.Node;
import com.breakersoft.plow.dispatcher.DispatchJob;
import com.breakersoft.plow.dispatcher.DispatchNode;
import com.breakersoft.plow.dispatcher.Dispatcher;
import com.breakersoft.plow.dispatcher.DispatcherThread;
import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.event.EventManagerImpl;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.service.DispatcherService;
import com.breakersoft.plow.service.JobLauncherService;
import com.breakersoft.plow.service.NodeService;
import com.breakersoft.plow.test.AbstractTest;

public class DispatchThreadTest extends AbstractTest {

    @Resource
    Dispatcher dispatcher;

    @Resource
    DispatcherService dispatcherService;

    @Resource
    JobLauncherService jobLaucherService;

    @Resource
    NodeService nodeService;

    @Resource
    EventManager eventManager;

    @Before
    public void init() {
        // Disable the event manager so we can add the job
        // to the dispatch thread on its own.
        ((EventManagerImpl) eventManager).setEnabled(false);
    }

    @Test
    public void testHandleJobLaunchedEvent() throws InterruptedException {

        JobLaunchEvent event = jobLaucherService.launch(getTestBlueprint());
        DispatchJob job = dispatcherService.getDispatchJob(event.getJob());

        DispatcherThread thread = new DispatcherThread(dispatcher, dispatcherService);
        assertEquals(0, thread.getWaitingJobs());

        thread.addJob(job);
        assertEquals(1, thread.getWaitingJobs());

        thread.update();
        assertEquals(0, thread.getWaitingJobs());
    }

    @Test
    public void testDispatchNode_NoJobs() throws InterruptedException {
        DispatcherThread thread = new DispatcherThread(dispatcher, dispatcherService);

        Node node = nodeService.createNode(getTestNodePing());
        DispatchNode dnode = dispatcherService.getDispatchNode(node.getName());
        thread.dispatch(dnode);
    }

    @Test
    public void testDispatchNode_Jobs() throws InterruptedException {

        DispatcherThread thread = new DispatcherThread(dispatcher, dispatcherService);
        JobLaunchEvent event = jobLaucherService.launch(getTestBlueprint());
        DispatchJob job = dispatcherService.getDispatchJob(event.getJob());

        Node node = nodeService.createNode(getTestNodePing());
        DispatchNode dnode = dispatcherService.getDispatchNode(node.getName());

        job.setLayers(dispatcherService.getDispatchLayers(job));


        thread.addJob(job);
        thread.update();
        thread.dispatch(dnode);
    }
}
