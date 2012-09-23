package com.breakersoft.plow.test.service;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.service.JobLauncherService;
import com.breakersoft.plow.test.AbstractTest;

public class JobLauncherServiceTests extends AbstractTest {

    @Resource
    JobLauncherService jobLauncherService;

    @Test
    public void testCreate() {
        JobLaunchEvent event = jobLauncherService.launch(getTestBlueprint());

        assertLayerCount(event.getJob(), 1);
        assertFrameCount(event.getJob(), 10);
    }
}
