package com.breakersoft.plow.test.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Node;
import com.breakersoft.plow.Proc;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dao.DispatchDao;
import com.breakersoft.plow.dao.ProcDao;
import com.breakersoft.plow.dispatcher.DispatchService;
import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchLayer;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.service.JobService;
import com.breakersoft.plow.service.NodeService;
import com.breakersoft.plow.test.AbstractTest;

@RunWith(SpringJUnit4ClassRunner.class)
public class ProcDaoTests extends AbstractTest {

    @Resource
    NodeService nodeService;

    @Resource
    DispatchDao dispatchDao;

    @Resource
    JobService jobService;

    @Resource
    DispatchService dispatchService;

    @Resource
    ProcDao procDao;;

    private Proc proc;

    private Task task;

    private DispatchNode dnode;

    @Before
    public void init() {

        // This is testing the creation of a proc.
        Node node =  nodeService.createNode(getTestNodePing());
        dnode = dispatchDao.getDispatchNode(node.getName());

        JobLaunchEvent event = jobService.launch(getTestJobSpec());
        DispatchJob djob = dispatchDao.getDispatchJob(event.getJob());

        for (DispatchLayer layer: dispatchDao.getDispatchLayers(djob, dnode)) {
            for (DispatchTask dtask: dispatchDao.getDispatchTasks(layer, dnode)) {
                proc = dispatchService.createProc(dnode, dtask);
                task = dtask;
                break;
            }
        }
    }

    @Test
    public void testCreate() {

        // Check to ensure the procs/memory were subtracted from the host.
        assertEquals(dnode.getCores(),
                simpleJdbcTemplate.queryForInt("SELECT int_idle_cores FROM node_dsp WHERE pk_node=?",
                        dnode.getNodeId()));

        assertEquals(dnode.getMemory(),
                simpleJdbcTemplate.queryForInt("SELECT int_free_memory FROM node_dsp WHERE pk_node=?",
                        dnode.getNodeId()));

    }

    @Test
    public void testGetProcById() {
        Proc otherProc = procDao.getProc(proc.getProcId());
        assertEquals(proc.getProcId(), otherProc.getProcId());
        assertEquals(proc.getHostname(), otherProc.getHostname());
        assertEquals(proc.getNodeId(), otherProc.getNodeId());
        assertEquals(proc.getQuotaId(), otherProc.getQuotaId());
        assertEquals(proc.getTaskId(), otherProc.getTaskId());
    }

    @Test
    public void testGetProcByTask() {
        Proc otherProc = procDao.getProc(task);
        assertEquals(proc.getProcId(), otherProc.getProcId());
        assertEquals(proc.getHostname(), otherProc.getHostname());
        assertEquals(proc.getNodeId(), otherProc.getNodeId());
        assertEquals(proc.getQuotaId(), otherProc.getQuotaId());
        assertEquals(proc.getTaskId(), otherProc.getTaskId());
    }

    @Test
    public void testDelete() {
        assertTrue(procDao.delete(proc));
        assertFalse(procDao.delete(proc));
    }

    public void testProcsByJob() {

    }

    public void testSetProcUnbooked() {

    }

}
