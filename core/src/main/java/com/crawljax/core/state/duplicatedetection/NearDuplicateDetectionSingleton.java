package com.crawljax.core.state.duplicatedetection;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NearDuplicateDetectionSingleton {
	private static NearDuplicateDetection ndd;
	private static int threshold;
	private static List<FeatureType> features = new ArrayList<FeatureType>();
	
	private static final Logger logger = LoggerFactory.getLogger(NearDuplicateDetectionCrawlHash32.class);
	
	public static NearDuplicateDetection getInstance() {
		if (ndd == null) {
			features.add(new FeatureShingles(3, FeatureSizeType.WORDS));
			ndd = new NearDuplicateDetectionCrawlHash32(threshold, features);
		}
		return ndd;
	}
	
	public static void setThreshold(int t) {
		logger.info("Set the threshold op {}", t);

		ndd = new NearDuplicateDetectionCrawlHash32(t, features);
	}
	public static int getThreshold() {
		return threshold;
	}
	public static void addFeature(FeatureType ft) {
		features.add(ft);
	}
}
