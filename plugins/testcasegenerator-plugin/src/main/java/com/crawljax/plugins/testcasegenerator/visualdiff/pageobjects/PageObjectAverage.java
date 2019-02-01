package com.crawljax.plugins.testcasegenerator.visualdiff.pageobjects;

import java.nio.ByteBuffer;

import org.opencv.core.Core;
import org.opencv.core.Mat;

/**
 * A {@code PageObject} that uses MD5 to compare object images.
 */
public class PageObjectAverage extends PageObject {

	/**
	 * The sensitivity of the hash to (color) change. The intensity of each color (RGB) is a value
	 * from 0-255. The sensitivity specifies the size of the buckets in which we group intensities,
	 * which means a sensitivity approaching 0 will detect minor changes, while a sensitivity of 255
	 * will give all images the same hash.
	 */
	private static final int SENSITIVITY = 15;

	PageObjectAverage(Mat image, int x, int y, int width, int height) {
		super(image, x, y, width, height);
	}

	/**
	 * @return the average color hash of the image as a byte array.
	 */
	@Override
	protected byte[] createImageHash() {
		double[] val = Core.mean(image).val;
		for (int i = 0; i < val.length; i++) {
			val[i] = Math.floor(val[i] / SENSITIVITY) * SENSITIVITY;
		}
		return toByteArray(val);
	}

	@Override
	protected String createImageText() {
		// TODO Auto-generated method stub
		return null;
	}

	public static byte[] toByteArray(double[] doubleArray) {
		int times = Double.SIZE / Byte.SIZE;
		byte[] bytes = new byte[doubleArray.length * times];
		for (int i = 0; i < doubleArray.length; i++) {
			ByteBuffer.wrap(bytes, i * times, times).putDouble(doubleArray[i]);
		}
		return bytes;
	}

}
