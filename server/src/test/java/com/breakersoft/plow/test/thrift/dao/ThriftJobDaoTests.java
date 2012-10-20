package com.breakersoft.plow.test.thrift.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.service.JobService;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.Blueprint;
import com.breakersoft.plow.thrift.JobFilter;
import com.breakersoft.plow.thrift.JobT;
import com.breakersoft.plow.thrift.dao.ThriftJobDao;

public class ThriftJobDaoTests extends AbstractTest {

    @Resource
    JobService jobService;

    @Resource
    ThriftJobDao thriftJobDao;

    @Test
    public void getJobs() {
        Blueprint bp = getTestBlueprint();
        jobService.launch(bp);

        assertTrue(thriftJobDao.getJobs(new JobFilter()).size() > 0);

        JobFilter f = new JobFilter();
        f.addToUser("lila");
        assertEquals(0, thriftJobDao.getJobs(f).size());

        f.addToUser("stella");
        assertEquals(1, thriftJobDao.getJobs(f).size());
    }

    @Test
    public void getJob() {
        Blueprint bp = getTestBlueprint();
        JobLaunchEvent event = jobService.launch(bp);

        JobT job = thriftJobDao.getJob(
                event.getJob().getJobId().toString());
        assertEquals(job.id, event.getJob().getJobId().toString());
    }
}
