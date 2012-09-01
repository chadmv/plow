package com.breakersoft.plow.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;

import com.breakersoft.plow.FrameSet;

public class FrameSetTests {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(FrameSetTests.class);

    @Test
    public void testContiguousRange() {
        FrameSet fs = new FrameSet("1-10");
        assertEquals(10, fs.size());
        assertEquals(1, fs.get(0));
        assertEquals(10, fs.get(9));

        fs = new FrameSet("1-10,15-20");
        assertEquals(16, fs.size());
        assertEquals(1, fs.get(0));
        assertEquals(20, fs.get(15));
    }

    @Test
    public void testSingleFrame() {
        FrameSet fs = new FrameSet("1");
        assertEquals(1, fs.size());
        assertEquals(1, fs.get(0));

        fs = new FrameSet("1,2");
        assertEquals(2, fs.size());
        assertEquals(1, fs.get(0));
        assertEquals(2, fs.get(1));
    }

    @Test
    public void testChunked() {
        FrameSet fs = new FrameSet("1-10x2");
        assertEquals(5, fs.size());
        assertEquals(1, fs.get(0));
        assertEquals(9, fs.get(4));
    }
}
