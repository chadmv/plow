package com.breakersoft.plow.test.dao;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Folder;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.dao.ClusterDao;
import com.breakersoft.plow.dao.DispatchDao;
import com.breakersoft.plow.dao.FolderDao;
import com.breakersoft.plow.dao.NodeDao;
import com.breakersoft.plow.dao.QuotaDao;
import com.breakersoft.plow.dispatcher.DispatchFolder;
import com.breakersoft.plow.dispatcher.DispatchJob;
import com.breakersoft.plow.dispatcher.DispatchProject;
import com.breakersoft.plow.service.JobLauncherService;
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
    JobLauncherService jobLauncherService;

    @Test
    public void testGetDispatchFolder() {
        Folder folder = folderDao.getDefaultFolder(testProject);
        DispatchFolder dfolder = dispatchDao.getDispatchFolder(folder);
        assertEquals(folder.getFolderId(), dfolder.getFolderId());
    }

    @Test
    public void testGetDispatchJob() {
        Job job = jobLauncherService.launch(getTestBlueprint());
        DispatchJob djob = dispatchDao.getDispatchJob(job);
        assertTrue(djob.getTier() == 0);
    }

    @Test
    public void testSortedProjects() {
        Node node =  nodeService.createNode(getTestNodePing());
        quotaDao.create(testProject, clusterDao.getCluster("unassigned"), 10, 15);
        List<DispatchProject> projects =  dispatchDao.getSortedProjectList(node);
        assertEquals(1, projects.size());
    }
}
