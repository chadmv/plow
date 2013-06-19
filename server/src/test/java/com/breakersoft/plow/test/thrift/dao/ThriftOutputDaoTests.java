package com.breakersoft.plow.test.thrift.dao;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Layer;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.JobSpecT;
import com.breakersoft.plow.thrift.dao.ThriftOutputDao;
import com.google.common.collect.Maps;

public class ThriftOutputDaoTests extends AbstractTest {

    @Resource
    ThriftOutputDao thriftOutputDao;

    @Test
    public void getJobOutputs() {
        JobSpecT spec = getTestJobSpec();
        JobLaunchEvent event = jobService.launch(spec);

        Layer layer = jobService.getLayer(event.getJob(), 0);
        Map<String,String> attrs = Maps.newHashMap();
        jobService.addLayerOutput(layer, "/foo/bar.#.exr", attrs);

        assertEquals(1, thriftOutputDao.getOutputs(event.getJob()).size());
    }

    @Test
    public void getOutputs() {
        JobSpecT spec = getTestJobSpec();
        JobLaunchEvent event = jobService.launch(spec);

        Layer layer = jobService.getLayer(event.getJob(), 0);
        Map<String,String> attrs = Maps.newHashMap();
        jobService.addLayerOutput(layer, "/foo/bar.#.exr", attrs);

        assertEquals(1, thriftOutputDao.getOutputs(layer).size());
    }
}
