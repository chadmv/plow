package com.breakersoft.plow.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.breakersoft.plow.FrameSet;

public class FrameSetTests {

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
    public void testChunkedX() {
        FrameSet fs = new FrameSet("1-10x2");
        assertEquals(5, fs.size());
        assertEquals(1, fs.get(0));
        assertEquals(9, fs.get(4));
    }

    @Test
    public void testChunkedY() {
        FrameSet fs = new FrameSet("1-10y2");
        assertEquals(5, fs.size());
        assertEquals(2, fs.get(0));
        assertEquals(10, fs.get(4));
    }

    @Test
    public void testChunkedStaggered() {
        FrameSet fs = new FrameSet("1-20:5");
        assertEquals(20, fs.size());
        assertEquals(1, fs.get(0));
        assertEquals(20, fs.get(19));
    }

    @Test
    public void testTimestamp() {
        System.out.println(System.currentTimeMillis());
    }

}
