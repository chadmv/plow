package com.breakersoft.plow.test.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Folder;
import com.breakersoft.plow.dao.FolderDao;
import com.breakersoft.plow.test.AbstractTest;

public class FolderDaoTests  extends AbstractTest {

    @Resource
    FolderDao folderDao;

    @Test
    public void testCreate() {
        Folder folder1 = folderDao.createFolder(testProject, "foo");
        Folder folder2 = folderDao.get(folder1.getFolderId());
        assertEquals(folder1, folder2);
    }
}
