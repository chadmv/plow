package com.breakersoft.plow.test.rrd;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.http.RrdGraphController;
import com.breakersoft.plow.test.AbstractTest;

public class RrdGraphControllerTests extends AbstractTest {

    @Resource
    RrdGraphController rrdGraphController;

    @Test
    public void testJoin() {
        long[] data = new long[] { 0L, 0L, 0L };
        assertEquals("0:0:0",RrdGraphController.join(data, ":"));
    }
}
