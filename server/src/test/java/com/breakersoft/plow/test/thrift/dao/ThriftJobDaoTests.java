package com.breakersoft.plow.test.thrift.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.JobFilterT;
import com.breakersoft.plow.thrift.JobSpecT;
import com.breakersoft.plow.thrift.JobT;
import com.breakersoft.plow.thrift.dao.ThriftJobDao;

public class ThriftJobDaoTests extends AbstractTest {

    @Resource
    ThriftJobDao thriftJobDao;

    @Test
    public void getJobs() {
        JobSpecT spec = getTestJobSpec();
        jobService.launch(spec);

        assertTrue(thriftJobDao.getJobs(new JobFilterT()).size() > 0);

        JobFilterT f = new JobFilterT();
        f.addToUser("lila");
        assertEquals(0, thriftJobDao.getJobs(f).size());

        f.addToUser("stella");
        assertEquals(1, thriftJobDao.getJobs(f).size());
    }

    @Test
    public void getJobsByProject() {
        JobSpecT spec = getTestJobSpec();
        jobService.launch(spec);

        assertTrue(thriftJobDao.getJobs(new JobFilterT()).size() > 0);

        JobFilterT f = new JobFilterT();
        f.addToProject("unittest1");
        assertEquals(0, thriftJobDao.getJobs(f).size());

        f = new JobFilterT();
        f.addToProject("unittest");
        assertEquals(1, thriftJobDao.getJobs(f).size());
    }

    @Test
    public void getJob() {
        JobSpecT spec = getTestJobSpec();
        JobLaunchEvent event = jobService.launch(spec);

        JobT job = thriftJobDao.getJob(
                event.getJob().getJobId().toString());
        assertEquals(job.id, event.getJob().getJobId().toString());
    }

    @Test
    public void getRunningJob() {
        JobSpecT spec = getTestJobSpec();
        JobLaunchEvent event = jobService.launch(spec);

        String jobName = event.getJobSpec().getName();

        JobT job = thriftJobDao.getRunningJob(jobName);
        assertEquals(job.name, jobName);
    }

    @Test
    public void getJobSpec() {
        JobSpecT spec1 = getTestJobSpec();
        JobLaunchEvent event = jobService.launch(spec1);
        JobSpecT spec2 = thriftJobDao.getJobSpec(event.getJob().getJobId());

        assertEquals(spec1.name, spec2.name);
    }

}
