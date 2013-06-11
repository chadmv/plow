package com.breakersoft.plow.test.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;

import com.breakersoft.plow.Task;
import com.breakersoft.plow.dao.TaskDao;
import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.event.EventManagerImpl;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.service.StateManager;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.TaskFilterT;
import com.breakersoft.plow.thrift.TaskState;
import com.google.common.eventbus.Subscribe;

public class StateManagerTests extends AbstractTest {

    @Resource
    StateManager stateManager;

    @Resource
    EventManager eventManager;

    @Resource
    TaskDao taskDao;

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

    @Test
    public void testEatAll() throws InterruptedException {
        JobLaunchEvent event = jobService.launch(getTestJobSpec());

        TaskFilterT filter = new TaskFilterT();
        filter.jobId = event.getJob().getJobId().toString();
        stateManager.eatTasks(filter);
        Thread.sleep(1000);
        assertTrue(jobShutdownEventHandled);
    }

    @Test
    public void testEatSingle() throws InterruptedException {
        JobLaunchEvent event = jobService.launch(getTestJobSpec());
        TaskFilterT filter = new TaskFilterT();
        filter.jobId = event.getJob().getJobId().toString();
        filter.limit = 1;

        Task t = taskDao.getTasks(filter).get(0);
        stateManager.eatTask(t, false);

        assertEquals(TaskState.EATEN.ordinal(),
                jdbc().queryForInt("SELECT int_state FROM plow.task WHERE pk_task=?", t.getTaskId()));
    }

    @Subscribe
    public void handleJobShutdownEvent(JobLaunchEvent event) {
        jobShutdownEventHandled = true;
    }
}
