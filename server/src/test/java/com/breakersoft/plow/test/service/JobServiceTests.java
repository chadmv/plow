package com.breakersoft.plow.test.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;

import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.event.EventManagerImpl;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.JobT;
import com.breakersoft.plow.thrift.PlowException;
import com.breakersoft.plow.thrift.RpcService;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;

public class JobServiceTests extends AbstractTest {

    @Resource
    EventManager eventManager;

    @Resource
    RpcService.Iface rpcService;

    private boolean jobLaunchEventHandled;

    @Before
    public void reset() {
        ((EventManagerImpl) eventManager).setEnabled(true);
        eventManager.register(this);
        jobLaunchEventHandled = false;
    }

    @Test
    public void testLaunchJob() {
        JobLaunchEvent event = jobService.launch(getTestJobSpec());
        assertLayerCount(event.getJob(), 1);
        assertTaskCount(event.getJob(), 10);
        assertTrue(jobLaunchEventHandled);
    }

    @Test
    public void testLaunchJobWithManualTasks() {
        eventManager.register(this);
        JobLaunchEvent event =
                jobService.launch(getTestJobSpecManualTasks("manual_test"));
        assertLayerCount(event.getJob(), 1);
        assertTaskCount(event.getJob(), 1);
        assertTrue(jobLaunchEventHandled);
    }

    @Test
    public void testLaunchJobWithAttrs() throws PlowException, TException {

        Map<String,String> attrs = Maps.newHashMap();
        attrs.put("scene", "abc");
        attrs.put("shot", "123");

        eventManager.register(this);
        JobLaunchEvent event =
                jobService.launch(getTestJobSpecWithAttrs("attrs_test", attrs));
        assertLayerCount(event.getJob(), 1);
        assertTaskCount(event.getJob(), 1);
        assertTrue(jobLaunchEventHandled);

        JobT job = rpcService.getJob(event.getJob().getJobId().toString());
        assertEquals("abc", job.attrs.get("scene"));
        assertEquals("123", job.attrs.get("shot"));

        attrs.clear();
        attrs.put("capt", "picard");
        attrs.put("cmdr", "riker");

        rpcService.setJobAttrs(job.id, attrs);

        job = rpcService.getJob(event.getJob().getJobId().toString());
        assertEquals("picard", job.attrs.get("capt"));
        assertEquals("riker", job.attrs.get("cmdr"));
    }

    @Subscribe
    public void handleJobLaunchEvent(JobLaunchEvent event) {
        jobLaunchEventHandled = true;
    }
}
