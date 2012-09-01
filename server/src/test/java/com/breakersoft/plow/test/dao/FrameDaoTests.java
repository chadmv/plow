package com.breakersoft.plow.test.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Frame;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.dao.FrameDao;
import com.breakersoft.plow.dao.JobDao;
import com.breakersoft.plow.dao.LayerDao;
import com.breakersoft.plow.json.Blueprint;
import com.breakersoft.plow.json.BlueprintLayer;
import com.breakersoft.plow.test.AbstractTest;

public class FrameDaoTests extends AbstractTest {

    @Resource
    FrameDao frameDao;

    @Resource
    LayerDao layerDao;

    @Resource
    JobDao jobDao;

    private Layer layer;

    private Frame frame;

    @Test
    public void testCreate() {
        Blueprint bp = getTestBlueprint();
        Job job = jobDao.create(testProject, bp);
        BlueprintLayer bl = bp.getLayers().get(0);
        layer = layerDao.create(job, bl, 0);
        frame = frameDao.create(layer, 1, 0);
    }

    @Test
    public void testGet() {
        testCreate();
        Frame f1 = frameDao.get(frame.getFrameId());
        assertEquals(frame, f1);

        Frame f2 = frameDao.get(layer, 1);
        assertEquals(frame, f1);
        assertEquals(f2, f1);
    }

}
