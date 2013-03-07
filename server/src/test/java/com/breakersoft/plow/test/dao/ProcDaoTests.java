package com.breakersoft.plow.test.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.Proc;
import com.breakersoft.plow.dao.DispatchDao;
import com.breakersoft.plow.dao.ProcDao;
import com.breakersoft.plow.dispatcher.DispatchService;
import com.breakersoft.plow.dispatcher.NodeDispatcher;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchableJob;
import com.breakersoft.plow.dispatcher.domain.DispatchableTask;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.test.AbstractTest;

@RunWith(SpringJUnit4ClassRunner.class)
public class ProcDaoTests extends AbstractTest {

    @Resource
    DispatchDao dispatchDao;

    @Resource
    DispatchService dispatchService;

    @Resource
    NodeDispatcher nodeDispatcher;

    @Resource
    ProcDao procDao;;

    private Proc proc;

    private DispatchableTask task;

    private DispatchNode dnode;

    @Before
    public void init() {

        // This is testing the creation of a proc.
        Node node =  nodeService.createNode(getTestNodePing());
        dnode = dispatchDao.getDispatchNode(node.getName());

        JobLaunchEvent event = jobService.launch(getTestJobSpec());
        DispatchableJob  djob = dispatchDao.getDispatchableJob(event.getJob());

        task = dispatchService.getDispatchableTasks(djob.getJobId(),dnode).get(0);
        proc = dispatchService.allocateProc(dnode, task);
    }

    @Test
    public void testGetProcById() {
        Proc otherProc = procDao.getProc(proc.getProcId());
        assertEquals(proc.getProcId(), otherProc.getProcId());
        assertEquals(proc.getHostname(), otherProc.getHostname());
        assertEquals(proc.getNodeId(), otherProc.getNodeId());
        assertEquals(proc.getTaskId(), otherProc.getTaskId());
    }

    @Test
    public void testGetProcByTask() {
        Proc otherProc = procDao.getProc(task);
        assertEquals(proc.getProcId(), otherProc.getProcId());
        assertEquals(proc.getHostname(), otherProc.getHostname());
        assertEquals(proc.getNodeId(), otherProc.getNodeId());
        assertEquals(proc.getTaskId(), otherProc.getTaskId());
    }

    @Test
    public void testDelete() {
        assertTrue(procDao.delete(proc));
        assertFalse(procDao.delete(proc));
    }

    @Test
    public void testProcsByJob() {
        Job job = jobService.getJob(task.getJobId().toString());
        List<Proc> procs = procDao.getProcs(job);
        assertEquals(1, procs.size());
        assertEquals(proc.getProcId(), procs.get(0).getProcId());
        assertTrue(procs.contains(proc));

    }

    public void testSetProcUnbooked() {

    }

}
