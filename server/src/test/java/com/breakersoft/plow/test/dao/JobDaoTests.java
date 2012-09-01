package com.breakersoft.plow.test.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.dao.JobDao;
import com.breakersoft.plow.dao.ProjectDao;
import com.breakersoft.plow.json.Blueprint;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.JobState;

public class JobDaoTests extends AbstractTest {
	
	@Resource
	JobDao jobDao;
	
	@Resource
	ProjectDao projectDao;
	
	@Test
	public void testCreate() {
		Project project = projectDao.create("test", "Test");
		jobDao.create(project, getTestBlueprint());
	}
	
	@Test
	public void testGetByNameAndState() {
		Project project = projectDao.create("test", "Test");
		Blueprint blueprint = getTestBlueprint();
		Job jobA = jobDao.create(project, blueprint);
		Job jobB = jobDao.get(blueprint.getFullJobName(), JobState.INITIALIZE);
		
		assertEquals(jobA, jobB);
		assertEquals(jobA.getProjectId(), jobB.getProjectId());
	}
	
	@Test
	public void testGetById() {
		Project project = projectDao.create("test", "Test");
		Blueprint blueprint = getTestBlueprint();
		Job jobA = jobDao.create(project, blueprint);
		Job jobB = jobDao.get(jobA.getJobId());
		
		assertEquals(jobA, jobB);
		assertEquals(jobA.getProjectId(), jobB.getProjectId());
	}
}
