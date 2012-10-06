package com.breakersoft.plow.test.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.dao.JobDao;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.Blueprint;
import com.breakersoft.plow.thrift.JobBp;
import com.breakersoft.plow.thrift.JobState;

public class JobDaoTests extends AbstractTest {

    @Resource
    JobDao jobDao;

    @Test
    public void testCreate() {
        jobDao.create(testProject, getTestBlueprint());
    }

    @Test
    public void testGetByNameAndState() {
        Blueprint bp = getTestBlueprint();
        JobBp job = bp.job;
        Job jobA = jobDao.create(testProject, bp);
        Job jobB = jobDao.get(job.getName(), JobState.INITIALIZE);

        assertEquals(jobA, jobB);
        assertEquals(jobA.getProjectId(), jobB.getProjectId());
    }

    @Test
    public void testGetById() {
        Blueprint bp = getTestBlueprint();
        Job jobA = jobDao.create(testProject, bp);
        Job jobB = jobDao.get(jobA.getJobId());

        assertEquals(jobA, jobB);
        assertEquals(jobA.getProjectId(), jobB.getProjectId());
    }
}
