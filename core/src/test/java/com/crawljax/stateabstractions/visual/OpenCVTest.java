package com.crawljax.stateabstractions.visual;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.opencv.core.Mat;

public class OpenCVTest {

    @Before
    public void setUp() {
        OpenCVLoad.load();
    }

    @Test
    public void simpleTest() {
        Mat mat = Mat.zeros(3, 3, 0);
        assertEquals(mat.cols(), 3);
    }
}
