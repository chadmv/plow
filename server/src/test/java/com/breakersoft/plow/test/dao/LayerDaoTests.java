package com.breakersoft.plow.test.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.dao.JobDao;
import com.breakersoft.plow.dao.LayerDao;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.Blueprint;
import com.breakersoft.plow.thrift.JobBp;
import com.breakersoft.plow.thrift.JobState;
import com.breakersoft.plow.thrift.LayerBp;

public class LayerDaoTests extends AbstractTest {

    @Resource
    LayerDao layerDao;

    @Resource
    JobDao jobDao;

    private Layer layer;

    @Test
    public void testCreate() {
        Blueprint bp = getTestBlueprint();
        Job job = jobDao.create(TEST_PROJECT, bp);
        LayerBp bl = bp.getLayers().get(0);
        layer = layerDao.create(job, bl, 0);
    }

    @Test
    public void testGetByName() {
        testCreate();
        Blueprint bp = getTestBlueprint();
        JobBp jbp = bp.job;
        Job job = jobDao.get(jbp.getName(), JobState.INITIALIZE);
        Layer layer1 = layerDao.get(job, "test_ls");
        assertEquals(layer, layer1);
    }

    @Test
    public void testGetById() {
        testCreate();
        Layer layer1 = layerDao.get(layer.getLayerId());
        assertEquals(layer, layer1);
    }
}
