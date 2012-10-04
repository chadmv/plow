package com.breakersoft.plow.test.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.dao.JobDao;
import com.breakersoft.plow.json.Blueprint;
import com.breakersoft.plow.test.AbstractTest;
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
        Blueprint blueprint = getTestBlueprint();
        Job jobA = jobDao.create(testProject, blueprint);
        Job jobB = jobDao.get(blueprint.getName(), JobState.INITIALIZE);

        assertEquals(jobA, jobB);
        assertEquals(jobA.getProjectId(), jobB.getProjectId());
    }

    @Test
    public void testGetById() {
        Blueprint blueprint = getTestBlueprint();
        Job jobA = jobDao.create(testProject, blueprint);
        Job jobB = jobDao.get(jobA.getJobId());

        assertEquals(jobA, jobB);
        assertEquals(jobA.getProjectId(), jobB.getProjectId());
    }
}
