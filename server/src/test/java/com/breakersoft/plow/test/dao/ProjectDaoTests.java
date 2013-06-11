package com.breakersoft.plow.test.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        Project project = projectDao.get("unittest");
        assertEquals(TEST_PROJECT, project);
    }

    @Test
    public void testGetById() {
        Project projecta = projectDao.get("unittest");
        assertEquals(TEST_PROJECT, projecta);
        Project projectb = projectDao.get(projecta.getProjectId());
        assertEquals(TEST_PROJECT, projectb);
    }

    @Test
    public void testSetActive() {
        Project project = projectDao.get("unittest");
        projectDao.setActive(project, false);
        boolean active = jdbc().queryForObject(
                "SELECT bool_active FROM plow.project WHERE pk_project=?", Boolean.class, project.getProjectId());
        assertFalse(active);
        projectDao.setActive(project, true);
        active = jdbc().queryForObject(
                "SELECT bool_active FROM plow.project WHERE pk_project=?", Boolean.class, project.getProjectId());
        assertTrue(active);
    }
}
