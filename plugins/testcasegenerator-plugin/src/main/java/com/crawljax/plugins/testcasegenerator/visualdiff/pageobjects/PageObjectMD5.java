package com.crawljax.plugins.testcasegenerator.visualdiff.pageobjects;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.opencv.core.Mat;

/**
 * A {@code PageObject} that uses MD5 to compare object images.
 */
public class PageObjectMD5 extends PageObject {

	PageObjectMD5(Mat image, int x, int y, int width, int height) {
		super(image, x, y, width, height);
	}

	/**
	 * @return the MD5 hash of the image as a byte array.
	 */
	@Override
	protected byte[] createImageHash() {

		byte[] hashbytes = null;

		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] imgbytes = new byte[(safeLongToInt(image.total())) * image.channels()];
			image.get(0, 0, imgbytes);
			hashbytes = digest.digest(imgbytes);

		} catch (NoSuchAlgorithmException e) {
			System.err.println("<PageObjectMD5::imageHash> NoSuchAlgorithmException");
		}

		return hashbytes;

	}

	@Override
	protected String createImageText() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * from http://answers.opencv.org/question/4761/mat-to-byte-array/
	 */
	private int safeLongToInt(long l) {
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(
			        l + " cannot be cast to int without changing its value.");
		}
		return (int) l;
	}

}
