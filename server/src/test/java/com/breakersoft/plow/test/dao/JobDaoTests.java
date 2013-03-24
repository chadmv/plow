package com.breakersoft.plow.test.dao;

import static org.junit.Assert.*;

import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.dao.JobDao;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.service.JobService;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.JobSpecT;
import com.breakersoft.plow.thrift.JobState;

public class JobDaoTests extends AbstractTest {

    @Resource
    JobDao jobDao;

    @Resource
    JobService jobService;

    @Test
    public void testCreate() {
        jobDao.create(TEST_PROJECT, getTestJobSpec());
    }

    @Test
    public void testSetMinCores() {
    	JobSpecT spec = getTestJobSpec();
        JobLaunchEvent event = jobService.launch(spec);
        jobDao.setMinCores(event.getJob(), 101);

        int value = jdbc().queryForInt(
                "SELECT int_min_cores FROM plow.job_dsp WHERE pk_job=?",
                event.getJob().getJobId());
        assertEquals(101, value);
    }

    @Test
    public void testSetMaxCores() {
    	JobSpecT spec = getTestJobSpec();
        JobLaunchEvent event = jobService.launch(spec);
        jobDao.setMaxCores(event.getJob(), 101);

        int value = jdbc().queryForInt(
                "SELECT int_max_cores FROM plow.job_dsp WHERE pk_job=?",
                event.getJob().getJobId());
        assertEquals(101, value);
    }

    @Test
    public void testGetByNameAndState() {
        JobSpecT spec = getTestJobSpec();
        Job jobA = jobDao.create(TEST_PROJECT, spec);
        Job jobB = jobDao.get(spec.getName(), JobState.INITIALIZE);

        assertEquals(jobA, jobB);
        assertEquals(jobA.getProjectId(), jobB.getProjectId());
    }

    @Test
    public void testGetById() {
        JobSpecT spec = getTestJobSpec();
        Job jobA = jobDao.create(TEST_PROJECT, spec);
        Job jobB = jobDao.get(jobA.getJobId());

        assertEquals(jobA, jobB);
        assertEquals(jobA.getProjectId(), jobB.getProjectId());
    }

    @Test
    public void testGetActiveById() {
        JobSpecT spec = getTestJobSpec();
        Job jobA = jobDao.create(TEST_PROJECT, spec);
        Job jobB = jobDao.getActive(jobA.getJobId());

        assertEquals(jobA, jobB);
        assertEquals(jobA.getProjectId(), jobB.getProjectId());
    }

    @Test
    public void testGetActiveByName() {
        JobSpecT spec = getTestJobSpec();
        Job jobA = jobDao.create(TEST_PROJECT, spec);
        Job jobB = jobDao.getActive(spec.getName());

        assertEquals(jobA, jobB);
        assertEquals(jobA.getProjectId(), jobB.getProjectId());
    }

    @Test
    public void testGetActiveByNameOrId() {
        JobSpecT spec = getTestJobSpec();
        Job jobA = jobDao.create(TEST_PROJECT, spec);
        Job jobB = jobDao.get(jobA.getJobId());
        Job jobC = jobDao.getByActiveNameOrId(spec.getName());

        assertEquals(jobA, jobB);
        assertEquals(jobA, jobC);
        assertEquals(jobA.getProjectId(), jobB.getProjectId());
    }


    @Test
    public void isFinished() {
        JobSpecT spec = getTestJobSpec();
        JobLaunchEvent event = jobService.launch(spec);
        assertFalse(jobDao.isFinished(event.getJob()));
        jobService.shutdown(event.getJob());
        assertTrue(jobDao.isFinished(event.getJob()));
    }

    @Test
    public void testShutdown() {
        JobSpecT spec = getTestJobSpec();
        JobLaunchEvent event = jobService.launch(spec);
        assertTrue(jobDao.shutdown(event.getJob()));
        assertFalse(jobDao.shutdown(event.getJob()));
    }

    @Test
    public void testPause() {
        JobSpecT spec = getTestJobSpec();
        JobLaunchEvent event = jobService.launch(spec);
        jobDao.setPaused(event.getJob(), true);
        assertTrue(jobDao.isPaused(event.getJob()));
        jobDao.setPaused(event.getJob(), false);
        assertFalse(jobDao.isPaused(event.getJob()));
    }

    @Test
    public void testUpdateMaxRss() {
        JobSpecT spec = getTestJobSpec();
        JobLaunchEvent event = jobService.launch(spec);
        UUID jobId = event.getJob().getJobId();

        assertTrue(jobDao.updateMaxRssMb(jobId, 1000));
        int rss = jdbc().queryForInt(
                "SELECT int_max_rss FROM job_ping WHERE pk_job=?", jobId);
        assertEquals(1000, rss);
        assertFalse(jobDao.updateMaxRssMb(jobId, 999));
        rss = jdbc().queryForInt(
                "SELECT int_max_rss FROM job_ping WHERE pk_job=?", jobId);
        assertEquals(1000, rss);
    }
}
