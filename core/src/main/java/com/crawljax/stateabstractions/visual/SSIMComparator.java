package com.crawljax.stateabstractions.visual;

import java.io.IOException;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;

import com.crawljax.core.Crawler;

public class SSIMComparator {

	public static double computeDistance(Mat page1, Mat page2) {

		Scalar mssim;
		try {
			mssim = SSIM.getMSSIM(page1, page2);
			return mssim.val[0];
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static double computeDistance(String image, String image2) {
		Mat img1 = Imgcodecs.imread(image);
		Mat img2 = Imgcodecs.imread(image2);
		
		return computeDistance(img1, img2);
	}

}
