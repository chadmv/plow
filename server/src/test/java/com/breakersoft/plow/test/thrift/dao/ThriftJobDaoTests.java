package com.breakersoft.plow.test.thrift.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.service.JobLauncherService;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.Blueprint;
import com.breakersoft.plow.thrift.JobFilter;
import com.breakersoft.plow.thrift.dao.ThriftJobDao;

public class ThriftJobDaoTests extends AbstractTest {

    @Resource
    JobLauncherService jobLauncherService;

    @Resource
    ThriftJobDao thriftJobDao;

    @Test
    public void getJobs() {
        Blueprint bp = getTestBlueprint();
        JobLaunchEvent event = jobLauncherService.launch(bp);

        assertTrue(thriftJobDao.getJobs(new JobFilter()).size() > 0);

        JobFilter f = new JobFilter();
        f.addToUser("lila");
        assertEquals(0, thriftJobDao.getJobs(f).size());

        f.addToUser("stella");
        assertEquals(1, thriftJobDao.getJobs(f).size());
    }
}
