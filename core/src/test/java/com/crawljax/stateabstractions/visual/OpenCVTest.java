package com.crawljax.stateabstractions.visual;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_java;
import org.junit.Before;
import org.junit.Test;
import org.opencv.core.Mat;

import static org.junit.Assert.assertEquals;

public class OpenCVTest {

	@Before
	public void setUp() {
		Loader.load(opencv_java.class);
	}

	@Test
	public void simpleTest() {
		Mat mat = Mat.zeros(3, 3, 0);
		assertEquals(mat.cols(), 3);
	}

}
