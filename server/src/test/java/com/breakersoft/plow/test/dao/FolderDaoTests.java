package com.breakersoft.plow.test.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Folder;
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
        Folder folder1 = folderDao.createFolder(TEST_PROJECT, "foo");
        Folder folder2 = folderDao.get(folder1.getFolderId());
        assertEquals(folder1, folder2);
    }

    @Test
    public void testGetDefaultFolder() {
        Folder folder1 = folderDao.createFolder(TEST_PROJECT, "test");
        projectDao.setDefaultFolder(TEST_PROJECT, folder1);
        Folder folder2 = folderDao.getDefaultFolder(TEST_PROJECT);
        assertEquals(folder1, folder2);
    }

    @Test
    public void testSetMinCores() {
        Folder folder1 = folderDao.createFolder(TEST_PROJECT, "foo");
        folderDao.setMinCores(folder1, 101);

        int value = jdbc().queryForInt(
                "SELECT int_cores_min FROM plow.folder_dsp WHERE pk_folder=?",  folder1.getFolderId());
        assertEquals(101, value);
    }

    @Test
    public void testSetMaxCores() {
        Folder folder1 = folderDao.createFolder(TEST_PROJECT, "foo");
        folderDao.setMaxCores(folder1, 101);

        int value = jdbc().queryForInt(
                "SELECT int_cores_max FROM plow.folder_dsp WHERE pk_folder=?", folder1.getFolderId());
        assertEquals(101, value);
    }

    @Test
    public void testSetName() {
        Folder folder1 = folderDao.createFolder(TEST_PROJECT, "foo");
        folderDao.setName(folder1, "bar");
        String name = jdbc().queryForObject(
                "SELECT str_name FROM plow.folder WHERE pk_folder=?", String.class, folder1.getFolderId());
        assertEquals("bar", name);
    }

    @Test
    public void testSet() {
        Folder folder1 = folderDao.createFolder(TEST_PROJECT, "foo");
        Folder folder2 = folderDao.get(folder1.getFolderId());
        assertEquals(folder1, folder2);
    }
}
