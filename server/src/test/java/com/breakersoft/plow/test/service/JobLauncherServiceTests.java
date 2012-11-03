package com.breakersoft.plow.test.service;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.service.JobService;
import com.breakersoft.plow.test.AbstractTest;

public class JobLauncherServiceTests extends AbstractTest {

    @Resource
    JobService jobService;

    @Test
    public void testCreate() {
        JobLaunchEvent event = jobService.launch(getTestJobSpec());

        assertLayerCount(event.getJob(), 1);
        assertFrameCount(event.getJob(), 10);
    }
}
