package com.breakersoft.plow.test.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Project;
import com.breakersoft.plow.dao.ProjectDao;
import com.breakersoft.plow.test.AbstractTest;

public class ProjectDaoTests extends AbstractTest {
	
	@Resource
	ProjectDao projectDao;
	
	@Test
	public void testCreate() {
		projectDao.create("test", "Test");
	}

	@Test
	public void testGet() {
		Project projectA = projectDao.create("test", "Test");
		Project projectB = projectDao.get("test");
		assertEquals(projectA, projectB);
	}
}
