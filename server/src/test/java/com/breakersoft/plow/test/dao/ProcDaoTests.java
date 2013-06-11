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
import com.breakersoft.plow.dispatcher.DispatchService;
import com.breakersoft.plow.dispatcher.NodeDispatcher;
import com.breakersoft.plow.dispatcher.dao.DispatchDao;
import com.breakersoft.plow.dispatcher.dao.ProcDao;
import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;
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

    private DispatchProc proc;

    private DispatchTask task;

    private DispatchNode dnode;

    private DispatchJob job;

    @Before
    public void init() {

        // This is testing the creation of a proc.
        Node node =  nodeService.createNode(getTestNodePing());
        dnode = dispatchDao.getDispatchNode(node.getName());

        JobLaunchEvent event = jobService.launch(getTestJobSpec("proc_tests", 2));
        job = new DispatchJob(event.getJob());

        task = dispatchService.getDispatchableTasks(job, dnode).get(0);
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
        Job job = jobService.getJob(task.getJobId());
        List<Proc> procs = procDao.getProcs(job);
        assertEquals(1, procs.size());
        assertEquals(proc.getProcId(), procs.get(0).getProcId());
        assertTrue(procs.contains(proc));
    }

    @Test
    public void assignAndUnassignProc() {
        assertTrue(procDao.unassign(proc));
        assertTrue(procDao.assign(proc, task));

        // Verify all running core counts.
        assertEquals(1, jdbc().queryForInt("SELECT int_cores_run FROM job_dsp WHERE pk_job=?", task.getJobId()));
        assertEquals(1, jdbc().queryForInt("SELECT int_cores_run FROM folder_dsp WHERE pk_folder=(SELECT pk_folder FROM job WHERE pk_job=?)", job.getJobId()));
        assertEquals(1, jdbc().queryForInt("SELECT int_cores_run FROM layer_dsp WHERE pk_layer=?", task.getLayerId()));
        assertEquals(1, jdbc().queryForInt("SELECT int_cores - int_idle_cores FROM node_dsp WHERE pk_node=?", proc.getNodeId()));
        assertEquals(1, jdbc().queryForInt("SELECT int_cores_run FROM quota WHERE pk_quota=?", proc.getQuotaId()));

        // Task is in a different layer
        DispatchTask nextTask = dispatchService.getDispatchableTasks(job, dnode).get(1);
        assertTrue(procDao.unassign(proc));
        assertTrue(procDao.assign(proc, nextTask));
        assertFalse(procDao.assign(proc, nextTask));

        // Verify all running core counts.  Basically all the same except the layer changes.
        assertEquals(1, jdbc().queryForInt("SELECT int_cores_run FROM job_dsp WHERE pk_job=?", task.getJobId()));
        assertEquals(1, jdbc().queryForInt("SELECT int_cores_run FROM folder_dsp WHERE pk_folder=(SELECT pk_folder FROM job WHERE pk_job=?)", job.getJobId()));
        assertEquals(0, jdbc().queryForInt("SELECT int_cores_run FROM layer_dsp WHERE pk_layer=?", task.getLayerId()));
        assertEquals(1, jdbc().queryForInt("SELECT int_cores_run FROM layer_dsp WHERE pk_layer=?", nextTask.getLayerId()));
        assertEquals(1, jdbc().queryForInt("SELECT int_cores - int_idle_cores FROM node_dsp WHERE pk_node=?", proc.getNodeId()));
        assertEquals(1, jdbc().queryForInt("SELECT int_cores_run FROM quota WHERE pk_quota=?", proc.getQuotaId()));
    }


    public void testSetProcUnbooked() {

    }

}
