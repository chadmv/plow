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
    public void testGet() {
        Project project = projectDao.get("test");
        assertEquals(testProject, project);
    }
}
