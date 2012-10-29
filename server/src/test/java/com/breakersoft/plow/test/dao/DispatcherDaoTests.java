package com.breakersoft.plow.test.dao;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Folder;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.dao.ClusterDao;
import com.breakersoft.plow.dao.DispatchDao;
import com.breakersoft.plow.dao.FolderDao;
import com.breakersoft.plow.dao.QuotaDao;
import com.breakersoft.plow.dispatcher.DispatchService;
import com.breakersoft.plow.dispatcher.domain.DispatchFolder;
import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchLayer;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchProject;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.rnd.thrift.RunTaskCommand;
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

    @Test
    public void testGetDispatchFolder() {
        Folder folder = folderDao.getDefaultFolder(TEST_PROJECT);
        DispatchFolder dfolder = dispatchDao.getDispatchFolder(folder.getFolderId());
        assertEquals(folder.getFolderId(), dfolder.getFolderId());
        assertEquals(0, dfolder.getRunCores());
        assertEquals(-1, dfolder.getMinCores());
        assertEquals(-1, dfolder.getMaxCores());
    }

    @Test
    public void testGetDispatchJob() {
        JobLaunchEvent event = jobService.launch(getTestBlueprint());
        DispatchJob djob = dispatchDao.getDispatchJob(event.getJob());
        assertTrue(djob.getTier() == 0);
        assertEquals(djob.getJobId(), event.getJob().getJobId());
    }

    @Test
    public void testSortedProjects() {
        Node node =  nodeService.createNode(getTestNodePing());
        List<DispatchProject> projects =  dispatchDao.getSortedProjectList(node);
        for (DispatchProject project: projects) {
            logger.info(project.getProjectId());
        }
        assertEquals(1, projects.size());
    }

    @Test
    public void testGetDispatchNode() {
        Node node =  nodeService.createNode(getTestNodePing());
        DispatchNode dnode = dispatchDao.getDispatchNode(node.getName());
    }

    @Test
    public void testGetDispatchProc() {
        Node node =  nodeService.createNode(getTestNodePing());
        DispatchNode dnode = dispatchDao.getDispatchNode(node.getName());

        JobLaunchEvent event = jobService.launch(getTestBlueprint());
        DispatchJob djob = dispatchDao.getDispatchJob(event.getJob());

        DispatchProc proc = null;

        for (DispatchLayer layer: dispatchDao.getDispatchLayers(djob, dnode)) {
            for (DispatchTask task: dispatchDao.getDispatchTasks(layer, dnode)) {
                proc = dispatchService.createProc(dnode, task);
                break;
            }
        }

        assertNotNull(proc);
        DispatchProc proc2 = dispatchDao.getDispatchProc(proc.getProcId());
        assertEquals(proc.getCores(), proc2.getCores());
        assertEquals(proc.getJobId(), proc2.getJobId());
        assertEquals(proc.getLayerId(), proc2.getLayerId());
        assertEquals(proc.getMemory(), proc2.getMemory());
        assertEquals(proc.getNodeId(), proc2.getNodeId());
        assertEquals(proc.getHostname(), proc2.getHostname());
        assertEquals(proc.getProcId(), proc2.getProcId());
        assertEquals(proc.getQuotaId(), proc2.getQuotaId());
        assertEquals(proc.getTags(), proc2.getTags());
        assertEquals(proc.getTaskId(), proc2.getTaskId());
        assertEquals(proc.getTaskName(), proc2.getTaskName());
    }

    @Test
    public void testGetRunTaskCommand() {
        Node node =  nodeService.createNode(getTestNodePing());
        DispatchNode dnode = dispatchDao.getDispatchNode(node.getName());

        JobLaunchEvent event = jobService.launch(getTestBlueprint());
        DispatchJob djob = dispatchDao.getDispatchJob(event.getJob());

        DispatchProc proc = null;
        DispatchTask task = null;
        for (DispatchLayer layer: dispatchDao.getDispatchLayers(djob, dnode)) {
            for (DispatchTask _task: dispatchDao.getDispatchTasks(layer, dnode)) {
                proc = dispatchService.createProc(dnode, _task);
                task = _task;
                break;
            }
        }
        assertNotNull(proc);
        assertNotNull(task);

        RunTaskCommand command = dispatchDao.getRunTaskCommand(task, proc);
        assertNotNull(command.command);
        assertNotNull(command.cores);
        assertNotNull(command.env);
        assertNotNull(command.jobId);
        assertNotNull(command.logFile);
        assertNotNull(command.procId);
        assertNotNull(command.taskId);
        assertNotNull(command.uid);
        assertNotNull(command.username);
    }

    @Test
    public void testGetDispatchTasks() {
        Node node =  nodeService.createNode(getTestNodePing());
        DispatchNode dnode = dispatchDao.getDispatchNode(node.getName());

        JobLaunchEvent event = jobService.launch(getTestBlueprint());
        DispatchJob djob = dispatchDao.getDispatchJob(event.getJob());

        for (DispatchLayer layer: dispatchDao.getDispatchLayers(djob, dnode)) {
            assertTrue(dispatchDao.getDispatchTasks(layer, dnode).size() > 0);
        }
    }

    @Test
    public void testGetDispatchLayers() {
        Node node =  nodeService.createNode(getTestNodePing());
        DispatchNode dnode = dispatchDao.getDispatchNode(node.getName());

        JobLaunchEvent event = jobService.launch(getTestBlueprint());
        DispatchJob djob = dispatchDao.getDispatchJob(event.getJob());

        dispatchDao.getDispatchLayers(djob, dnode);
    }
}
