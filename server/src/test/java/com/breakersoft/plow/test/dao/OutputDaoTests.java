package com.breakersoft.plow.test.dao;

import static org.junit.Assert.*;

import java.util.Map;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.Output;
import com.breakersoft.plow.dao.JobDao;
import com.breakersoft.plow.dao.LayerDao;
import com.breakersoft.plow.dao.OutputDao;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.JobSpecT;
import com.breakersoft.plow.thrift.LayerSpecT;
import com.google.common.collect.Maps;

public class OutputDaoTests extends AbstractTest {

    @Resource
    private JobDao jobDao;

    @Resource
    private LayerDao layerDao;

    @Resource
    private OutputDao outputDao;

    private Layer layer;

    private Job job;

    private static Map<String,String> attrs = Maps.newHashMap();
    static {
        attrs.put("res_x", "1024");
        attrs.put("res_y", "768");
    }

    @Before
    public void testCreate() {
        JobSpecT spec = getTestJobSpec();
        job = jobDao.create(TEST_PROJECT, spec, false);
        LayerSpecT bl = spec.getLayers().get(0);
        layer = layerDao.create(job, bl, 0);
    }

    @Test
    public void testAddOutput() {

        Output out1 = outputDao.addOutput(layer, "/tmp/foo_v1.#.exr", attrs);
        assertEquals(out1.getPath(),"/tmp/foo_v1.#.exr");
        assertEquals(out1.getAttrs(), attrs);

        Output out2 = outputDao.addOutput(layer, "/tmp/bar_v2.#.exr", attrs);
        assertEquals(out2.getPath(),"/tmp/bar_v2.#.exr");
        assertEquals(out2.getAttrs(), attrs);

        assertEquals(2, jdbc().queryForInt(
                "SELECT COUNT(1) FROM output WHERE pk_layer=?", layer.getLayerId()));
    }

    @Test
    public void testGetAttrs() {

        Output out1a = outputDao.addOutput(layer, "/tmp/foo_v1.#.exr", attrs);
        Map<String,String> _attrs = outputDao.getAttrs(out1a.getId());
        assertEquals(attrs, _attrs);
    }

    @Test
    public void testSetAttrs() {

        Map<String,String> new_attrs = Maps.newHashMap();
        new_attrs.put("foo", "a");
        new_attrs.put("bar", "b");

        Output out1a = outputDao.addOutput(layer, "/tmp/foo_v1.#.exr", attrs);
        outputDao.setAttrs(out1a.getId(), new_attrs);
        assertEquals(new_attrs, outputDao.getAttrs(out1a.getId()));
    }

    @Test
    public void testUpdateAttrs() {

        Map<String,String> new_attrs = Maps.newHashMap();
        new_attrs.put("foo", "a");
        new_attrs.put("bar", "b");

        Output out1a = outputDao.addOutput(layer, "/tmp/foo_v1.#.exr", attrs);
        outputDao.updateAttrs(out1a.getId(), new_attrs);

        new_attrs.putAll(attrs);
        assertEquals(new_attrs, outputDao.getAttrs(out1a.getId()));
    }
}
