package com.breakersoft.plow.test.dispatcher.dao;

import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;

import com.breakersoft.plow.dao.ProcDao;
import com.breakersoft.plow.dispatcher.DispatchDao;
import com.breakersoft.plow.dispatcher.dao.DispatchTaskDao;
import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.TaskState;

public class DispatchTaskDaoTest extends AbstractTest {

    @Resource
    DispatchTaskDao dispatchTaskDao;

    @Resource
    DispatchDao dispatchDao;

    @Resource
    ProcDao procDao;

    DispatchNode node;
    DispatchJob job;
    List<DispatchTask> tasks;

    @Before
    public void before() {
        node = dispatchDao.getDispatchNode(
                nodeService.createNode(getTestNodePing()).getName());
        JobLaunchEvent event = jobService.launch(getTestJobSpec());
        job = new DispatchJob(event.getJob());
        tasks = dispatchDao.getDispatchableTasks(job, node);
    }

    @Test
    public void testStart() {
        assertTrue(dispatchTaskDao.reserve(tasks.get(0)));
        procDao.create(node, tasks.get(0));
        assertTrue(dispatchTaskDao.start(tasks.get(0)));
    }

    @Test
    public void testStop() {

        dispatchTaskDao.stop(tasks.get(0), TaskState.SUCCEEDED);
    }

    @Test
    public void testReserve() {

        dispatchTaskDao.reserve(tasks.get(0));
    }

    @Test
    public void testUnreserve() {

        dispatchTaskDao.unreserve(tasks.get(0));
    }


}
