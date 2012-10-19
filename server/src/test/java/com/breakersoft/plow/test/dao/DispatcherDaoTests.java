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
import com.breakersoft.plow.dispatcher.domain.DispatchFolder;
import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProject;
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

    @Test
    public void testGetDispatchFolder() {
        Folder folder = folderDao.getDefaultFolder(TEST_PROJECT);
        DispatchFolder dfolder = dispatchDao.getDispatchFolder(folder.getFolderId());
        assertEquals(folder.getFolderId(), dfolder.getFolderId());
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
    public void testGetDispatchHost() {
        Node node =  nodeService.createNode(getTestNodePing());
        DispatchNode dnode = dispatchDao.getDispatchNode(node.getName());
    }

    @Test
    public void testGetFrames() {
        Node node =  nodeService.createNode(getTestNodePing());
        DispatchNode dnode = dispatchDao.getDispatchNode(node.getName());

        JobLaunchEvent event = jobService.launch(getTestBlueprint());
        DispatchJob djob = dispatchDao.getDispatchJob(event.getJob());
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
