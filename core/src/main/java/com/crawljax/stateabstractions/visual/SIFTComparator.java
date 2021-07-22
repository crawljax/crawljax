package com.crawljax.stateabstractions.visual;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.xfeatures2d.SIFT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.WebDriverBrowserBuilder;
import com.crawljax.core.Crawler;


public class SIFTComparator {
	private static final Logger LOGGER = LoggerFactory.getLogger(SIFTComparator.class);

	/* The threshold ratio used for the distance (Lowe's ratio test). */
	static float nndrRatio = 0.7f;

	/* The similarity threshold to decide whether two images are the same. */
	static float treshold = 0.05f;
	
	
	public static double computeDistance(String page1, String page2) {
		Mat mat1 = Imgcodecs.imread(page1);
		Mat mat2 = Imgcodecs.imread(page2);
		try {
			return computeDistance(mat1, mat2);
		}catch(Exception ex) {
			LOGGER.error("Error computing distance between {} and {}", page1, page2);
			return 0;
		}
		
		
	}
	public static double computeDistance(Mat page1, Mat page2) {
		
		return SIFTMatcher(page1, page2);
	}

	public static Double SIFTMatcher(Mat mat, Mat mat2) {
		Mat img1 = mat;
		Mat img2 = mat2;

		if (img1.empty() || img2.empty()) {
			System.err.println("Cannot read images!");
			System.exit(0);
		}

		/* Detect the key-points using SIFT detector and compute the descriptors. */
		SIFT detector = SIFT.create();

		MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
		MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
		Mat descriptors1 = new Mat();
		Mat descriptors2 = new Mat();

		detector.detectAndCompute(img1, new Mat(), keypoints1, descriptors1);
		detector.detectAndCompute(img2, new Mat(), keypoints2, descriptors2);
		
		/* Match descriptors with a BRUTEFORCE based matcher (NORM_L2). */
		DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
		List<MatOfDMatch> knnMatches = new ArrayList<>();
		matcher.knnMatch(descriptors1, descriptors2, knnMatches, 2);
		
//		System.out.println("knnMatches: " + knnMatches.size());

		List<DMatch> listOfGoodMatches = new ArrayList<>();
		for (int i = 0; i < knnMatches.size(); i++) {
			if (knnMatches.get(i).rows() > 1) {
				DMatch[] matches = knnMatches.get(i).toArray();
				if (matches[0].distance < nndrRatio * matches[1].distance) {
					listOfGoodMatches.add(matches[0]);
				}
			}
		}
		MatOfDMatch goodMatches = new MatOfDMatch();
		goodMatches.fromList(listOfGoodMatches);
	
//		System.out.println("goodMatches: " + listOfGoodMatches.size());
		
//		/* Draw matches. */
//		Mat imgMatches = new Mat();
//		Features2d.drawMatches(img1, keypoints1, img2, keypoints2, goodMatches, imgMatches, Scalar.all(-1), Scalar.all(-1), new MatOfByte(),
//				Features2d.NOT_DRAW_SINGLE_POINTS);
//		/* Show detected matches. */
//		HighGui.imshow("Good Matches", imgMatches);
//		HighGui.waitKey(0);
		
		img1.release();
		img2.release();
		keypoints1.release();
		keypoints2.release();
		descriptors1.release();
		descriptors2.release();

		double similarity = 1.0 * listOfGoodMatches.size() / knnMatches.size() * 100;
		return similarity;
	}

}
