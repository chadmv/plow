package com.breakersoft.plow.test.service;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.service.ProjectService;
import com.breakersoft.plow.test.AbstractTest;

public class ProjectServiceTests extends AbstractTest {

    @Resource
    ProjectService projectService;

    @Test
    public void createProject() {
        projectService.createProject("The Lion, The Witch, and The Wardrobe", "lww");
    }
}
