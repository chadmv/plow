package com.breakersoft.plow.test.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.FrameRange;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.dao.JobDao;
import com.breakersoft.plow.dao.LayerDao;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.JobSpecT;
import com.breakersoft.plow.thrift.JobState;
import com.breakersoft.plow.thrift.LayerSpecT;
import com.breakersoft.plow.util.JdbcUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
    public void testAddOutput() {
        testCreate();

        Map<String,String> attrs = Maps.newHashMap();
        attrs.put("res_x", "1024");
        attrs.put("res_y", "768");

        layerDao.addOutput(layer, "/tmp/foo_v1.#.exr", attrs);
        layerDao.addOutput(layer, "/tmp/bar_v2.#.exr", attrs);

        jdbc().queryForInt(
                "SELECT COUNT(1) FROM output WHERE pk_layer=?", layer.getLayerId());
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

    @Test
    public void isFinished() {

        JobSpecT spec = getTestJobSpec();
        JobLaunchEvent event = jobService.launch(spec);

        layer = jobService.getLayer(event.getJob(), 0);
        job = event.getJob();
        assertFalse(layerDao.isFinished(layer));
    }

    @Test
    public void setLayerMinCores() {
        testCreate();
        layerDao.setMinCores(layer, 8);
        int value = jdbc().queryForInt(
                "SELECT int_cores_min FROM plow.layer WHERE pk_layer=?", layer.getLayerId());
        assertEquals(8, value);
    }

    @Test
    public void setLayerMaxCores() {
        testCreate();
        layerDao.setMaxCores(layer, 8);
        int value = jdbc().queryForInt(
                "SELECT int_cores_max FROM plow.layer WHERE pk_layer=?", layer.getLayerId());
        assertEquals(8, value);
    }

    @Test
    public void setLayerMinRam() {
        testCreate();
        layerDao.setMinRam(layer, 8);
        int value = jdbc().queryForInt(
                "SELECT int_ram_min FROM plow.layer WHERE pk_layer=?", layer.getLayerId());
        assertEquals(8, value);
    }

    @Test
    public void setLayerTags() {
        testCreate();
        layerDao.setTags(layer, Lists.newArrayList("tag1", "tag2", "tag3", "tag1"));

        List<String> tags = jdbc().query(
                "SELECT unnest(str_tags) FROM plow.layer WHERE pk_layer=?", JdbcUtils.STRING_MAPPER, layer.getLayerId());
        assertEquals(3, tags.size());
        assertTrue(tags.contains("tag1"));
        assertTrue(tags.contains("tag2"));
        assertTrue(tags.contains("tag3"));
    }

    @Test
    public void setLayerThreadable() {
        testCreate();
        layerDao.setThreadable(layer, false);

        boolean value = jdbc().queryForObject(
                "SELECT bool_threadable FROM plow.layer WHERE pk_layer=?", Boolean.class, layer.getLayerId());

        assertFalse(value);

        layerDao.setThreadable(layer, true);
        value = jdbc().queryForObject(
                "SELECT bool_threadable FROM plow.layer WHERE pk_layer=?", Boolean.class, layer.getLayerId());
    }
}
