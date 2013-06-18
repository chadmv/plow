package com.breakersoft.plow.test.thrift.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;

import com.breakersoft.plow.dispatcher.DispatchService;
import com.breakersoft.plow.dispatcher.dao.DispatchDao;
import com.breakersoft.plow.dispatcher.dao.DispatchTaskDao;
import com.breakersoft.plow.dispatcher.dao.ProcDao;
import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.service.JobService;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.JobSpecT;
import com.breakersoft.plow.thrift.ProcFilterT;
import com.breakersoft.plow.thrift.ProcT;
import com.breakersoft.plow.thrift.dao.ThriftProcDao;

public class ThriftProcDaoTests extends AbstractTest {

    @Resource
    JobService jobService;

    @Resource
    ThriftProcDao thriftProcDao;

    @Resource
    DispatchTaskDao dispatchTaskDao;

    @Resource
    DispatchDao dispatchDao;

    @Resource
    ProcDao procDao;

    @Resource
    DispatchService dispatchService;

    private DispatchNode node;
    private DispatchJob job;
    private DispatchProc proc;
    private DispatchTask task;
    private JobLaunchEvent event;

    @Before
    public void init() {
        JobSpecT spec = getTestJobSpec();
        event = jobService.launch(spec);
        node = dispatchDao.getDispatchNode(
                nodeService.createNode(getTestNodePing()).getName());

        job = new DispatchJob(event.getJob());
        task = dispatchTaskDao.getDispatchableTasks(job, node, 10).get(0);

        assertTrue(dispatchTaskDao.reserve(task));
        proc = dispatchService.allocateProc(node, task);
    }

    @Test
    public void testGetProc() {
        ProcT proct = thriftProcDao.getProc(proc.getProcId());
        assertEquals(proct.id, proc.getProcId().toString());
    }

    @Test
    public void testGetProcsByProject() {
        ProcFilterT filter = new ProcFilterT();
        filter.addToProjectIds(TEST_PROJECT.getProjectId().toString());
        assertEquals(1, thriftProcDao.getProcs(filter).size());
    }

    @Test
    public void testGetProcsByFolder() {
        ProcFilterT filter = new ProcFilterT();
        filter.addToFolderIds(event.getFolder().getFolderId().toString());
        assertEquals(1, thriftProcDao.getProcs(filter).size());
    }

    @Test
    public void testGetProcsByJob() {
        ProcFilterT filter = new ProcFilterT();
        filter.addToJobIds(job.getJobId().toString());
        assertEquals(1, thriftProcDao.getProcs(filter).size());
    }

    @Test
    public void testGetProcsByLayer() {
        ProcFilterT filter = new ProcFilterT();
        filter.addToLayerIds(task.getLayerId().toString());
        assertEquals(1, thriftProcDao.getProcs(filter).size());
    }

    @Test
    public void testGetProcsByTask() {
        ProcFilterT filter = new ProcFilterT();
        filter.addToTaskIds(task.getTaskId().toString());
        assertEquals(1, thriftProcDao.getProcs(filter).size());
    }

    @Test
    public void testGetProcsByCluster() {
        ProcFilterT filter = new ProcFilterT();
        filter.addToClusterIds(node.getClusterId().toString());
        assertEquals(1, thriftProcDao.getProcs(filter).size());
    }
}
