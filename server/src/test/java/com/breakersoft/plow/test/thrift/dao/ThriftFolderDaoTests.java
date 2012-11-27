package com.breakersoft.plow.test.thrift.dao;

import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.FolderT;
import com.breakersoft.plow.thrift.JobSpecT;
import com.breakersoft.plow.thrift.dao.ThriftFolderDao;

public class ThriftFolderDaoTests extends AbstractTest {

    @Resource
    ThriftFolderDao thriftFolderDao;

    @Test
    public void testGetFoldersByProject() {
        JobSpecT spec = getTestJobSpec();
        jobService.launch(spec);
        thriftFolderDao.getFolders(TEST_PROJECT);
    }

    @Test
    public void testGetFolderById() {

        JobSpecT spec = getTestJobSpec();
        jobService.launch(spec);

        FolderT folder = thriftFolderDao.get(
                UUID.fromString("00000000-0000-0000-0000-000000000000"));

    }
}
