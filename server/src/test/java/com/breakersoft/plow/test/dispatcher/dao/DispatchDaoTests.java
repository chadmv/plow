package com.breakersoft.plow.test.dispatcher.dao;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;

import com.breakersoft.plow.dispatcher.DispatchService;
import com.breakersoft.plow.dispatcher.dao.DispatchDao;
import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.test.AbstractTest;

public class DispatchDaoTests extends AbstractTest {

    @Resource
    DispatchDao dispatchDao;

    @Resource
    DispatchService dispatchService;

    DispatchNode node;
    DispatchJob job;
    List<DispatchTask> tasks;

    @Before
    public void init() {
        node = dispatchDao.getDispatchNode(
                nodeService.createNode(getTestNodePing()).getName());
        JobLaunchEvent event = jobService.launch(getTestJobSpec());
        job = new DispatchJob(event.getJob());
        tasks = dispatchService.getDispatchableTasks(job, node, 10);
    }

    @Test
    public void testGetDeallocatedProcs() {
         DispatchProc proc = dispatchService.allocateProc(node, tasks.get(0));
         dispatchService.markAsDeallocated(proc);
         List<DispatchProc> procs = dispatchDao.getDeallocatedProcs();
         assertEquals(1, procs.size());
    }

}
