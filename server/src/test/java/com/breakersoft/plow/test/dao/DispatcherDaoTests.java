package com.breakersoft.plow.test.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;

import com.breakersoft.plow.Folder;
import com.breakersoft.plow.dao.ClusterDao;
import com.breakersoft.plow.dao.DispatchDao;
import com.breakersoft.plow.dao.FolderDao;
import com.breakersoft.plow.dao.QuotaDao;
import com.breakersoft.plow.dispatcher.DispatchResult;
import com.breakersoft.plow.dispatcher.DispatchService;
import com.breakersoft.plow.dispatcher.NodeDispatcher;
import com.breakersoft.plow.dispatcher.domain.DispatchFolder;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchProject;
import com.breakersoft.plow.dispatcher.domain.DispatchableFolder;
import com.breakersoft.plow.dispatcher.domain.DispatchableJob;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.service.JobService;
import com.breakersoft.plow.service.NodeService;
import com.breakersoft.plow.test.AbstractTest;

public class DispatcherDaoTests extends AbstractTest {

    @Resource
    DispatchDao dispatchDao;

    @Resource
    FolderDao folderDao;

    @Resource
    NodeService nodeService;

    @Resource
    ClusterDao clusterDao;

    @Resource
    QuotaDao quotaDao;

    @Resource
    JobService jobService;

    @Resource
    DispatchService dispatchService;

    @Resource
    NodeDispatcher nodeDispatcher;

    DispatchNode node;

    DispatchableJob job;

    @Before
    public void before() {
        node = dispatchDao.getDispatchNode(
                nodeService.createNode(getTestNodePing()).getName());

        JobLaunchEvent event = jobService.launch(getTestJobSpec());
        job = dispatchDao.getDispatchableJob(event.getJob());
    }

    @Test
    public void testGetDispatchFolder() {
        Folder folder = folderDao.getDefaultFolder(TEST_PROJECT);
        DispatchableFolder dfolder = dispatchDao.getDispatchableFolder(folder.getFolderId());

    }

    @Test
    public void testGetDispatchableJob() {
        DispatchableJob djob = dispatchDao.getDispatchableJob(job);
        assertTrue(djob.tier == 0);
        djob.minCores = 100;
        assertEquals(200, djob.incrementAndGetCores(200));
        assertEquals(2, (int)djob.tier);
    }

    @Test
    public void testSortedProjects() {
        List<DispatchProject> projects =  dispatchDao.getSortedProjectList(node);
        assertEquals(1, projects.size());
    }

    @Test
    public void testGetDispatchNode() {
        DispatchNode dnode = dispatchDao.getDispatchNode(node.getName());
    }

    @Test
    public void testGetDispatchProc() {

        DispatchResult result = new DispatchResult(node);
        result.isTest = true;
        nodeDispatcher.dispatch(result, node);

        for (DispatchProc proc: result.procs) {
            DispatchProc dbProc = dispatchDao.getDispatchProc(proc.getProcId());
            assertEquals(proc.getProcId(), dbProc.getProcId());
            assertEquals(proc.getJobId(), dbProc.getJobId());
            assertEquals(proc.getNodeId(), dbProc.getNodeId());
            assertEquals(proc.getTaskId(), dbProc.getTaskId());
            assertEquals(proc.getIdleCores(), proc.getIdleCores());
            assertEquals(proc.getIdleRam(), proc.getIdleRam());
        }
    }

    @Test
    public void testGetRunTaskCommand() {


    }

    @Test
    public void testGetDispatchTasks() {


    }
}
