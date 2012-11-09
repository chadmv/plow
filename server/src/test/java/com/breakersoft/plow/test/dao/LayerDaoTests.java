package com.breakersoft.plow.test.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.FrameRange;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.dao.JobDao;
import com.breakersoft.plow.dao.LayerDao;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.JobSpecT;
import com.breakersoft.plow.thrift.LayerSpecT;
import com.breakersoft.plow.thrift.JobState;

public class LayerDaoTests extends AbstractTest {

    @Resource
    LayerDao layerDao;

    @Resource
    JobDao jobDao;

    private Layer layer;

    private Job job;

    @Test
    public void testCreate() {
        JobSpecT spec = getTestJobSpec();
        job = jobDao.create(TEST_PROJECT, spec);
        LayerSpecT bl = spec.getLayers().get(0);
        layer = layerDao.create(job, bl, 0);
    }

    @Test
    public void testGetByName() {
        testCreate();
        JobSpecT spec = getTestJobSpec();
        Job job = jobDao.get(spec.getName(), JobState.INITIALIZE);
        Layer layer1 = layerDao.get(job, "test_ls");
        assertEquals(layer, layer1);
    }

    @Test
    public void testGetById() {
        testCreate();
        Layer layer1 = layerDao.get(layer.getLayerId());
        assertEquals(layer, layer1);
    }

    @Test
    public void testGetByIndex() {
        testCreate();
        Layer layer1 = layerDao.get(job, 0);
        assertEquals(layer, layer1);
    }

    @Test
    public void testGetFrameRange() {
        testCreate();
        FrameRange frange = layerDao.getFrameRange(layer);
        assertEquals(10, frange.frameSet.size());
    }

}
