package com.crawljax.core.state.duplicatedetection;

import java.util.ArrayList;
import java.util.List;

public class NearDuplicateDetectionSingleton {
	private static NearDuplicateDetection ndd;
	private static int threshold;
	private static List<FeatureType> features = new ArrayList<FeatureType>();
	
	static {
		features.add(new FeatureShingles(3, FeatureSizeType.WORDS));
	}
	
	public static NearDuplicateDetection getInstance() {
		if (ndd == null) {
			ndd = new NearDuplicateDetectionCrawlHash32(threshold, features);
		}
		return ndd;
	}
	
	public static void setThreshold(int t) {
		threshold = t;
		resetInstance();
	}
	public static int getThreshold() {
		return threshold;
	}
	public static void addFeature(FeatureType ft) {
		features.add(ft);
		resetInstance();
	}
	
	public static void resetInstance() {
		ndd = null;
	}
}
