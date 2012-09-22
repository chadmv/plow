package com.breakersoft.plow.test.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Folder;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.dao.FolderDao;
import com.breakersoft.plow.dao.ProjectDao;
import com.breakersoft.plow.test.AbstractTest;

public class FolderDaoTests extends AbstractTest {

    @Resource
    FolderDao folderDao;

    @Resource
    ProjectDao projectDao;

    @Test
    public void testCreate() {
        Folder folder1 = folderDao.createFolder(testProject, "foo");
        Folder folder2 = folderDao.get(folder1.getFolderId());
        assertEquals(folder1, folder2);
    }

    @Test
    public void testGetDefaultFolder() {
        Project project = projectDao.get("test");
        Folder folder1 = folderDao.createFolder(project, "test");
        projectDao.setDefaultFolder(project, folder1);
        Folder folder2 = folderDao.getDefaultFolder(project);
        assertEquals(folder1, folder2);
    }
}
