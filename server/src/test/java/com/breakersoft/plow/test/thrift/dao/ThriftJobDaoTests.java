package com.breakersoft.plow.test.thrift.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.service.JobLauncherService;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.Blueprint;
import com.breakersoft.plow.thrift.JobFilter;
import com.breakersoft.plow.thrift.RpcDataService;

public class ThriftJobDaoTests extends AbstractTest {

    @Resource
    JobLauncherService jobLauncherService;

    @Resource
    RpcDataService rpcDataService;

    @Test
    public void getJobs() {
        Blueprint bp = getTestBlueprint();
        JobLaunchEvent event = jobLauncherService.launch(bp);

        assertEquals(1, rpcDataService.getJobs(new JobFilter()).size());

        JobFilter f = new JobFilter();
        f.addToUser("chambers");
        assertEquals(0, rpcDataService.getJobs(f).size());

        f.addToUser("stella");
        assertEquals(1, rpcDataService.getJobs(f).size());
    }
}
