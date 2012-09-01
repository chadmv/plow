package com.breakersoft.plow.test.service;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.service.JobLauncherService;
import com.breakersoft.plow.test.AbstractTest;

public class JobLauncherServiceTests extends AbstractTest {

    @Resource
    JobLauncherService jobLauncherService;

    @Test
    public void testCreate() {
        Job job = jobLauncherService.launch(getTestBlueprint());

        assertLayerCount(job, 1);
        assertFrameCount(job, 10);
    }
}
