package com.crawljax.core.state.duplicatedetection;

import java.util.ArrayList;
import java.util.List;

public class NearDuplicateDetectionSingleton {
	private static NearDuplicateDetection ndd;
	private static double threshold;
	private static List<FeatureType> features = new ArrayList<FeatureType>();
	
	/*
	static {
	//	features.add(new FeatureShingles(3, FeatureSizeType.WORDS));
	}
	*/
	
	public static NearDuplicateDetection getInstance() {
		if (ndd == null) {
			ndd = new NearDuplicateDetectionBroder32(threshold, features);
		}
		return ndd;
	}
	
	public static void setThreshold(double t) {
		threshold = t;
		resetInstance();
	}
	public static double getThreshold() {
		return threshold;
	}
	
	public static List<FeatureType> getFeatures() {
		return ndd.getFeatures();
	}
	
	public static void addFeature(FeatureType ft) {
		features.add(ft);
		resetInstance();
	}
	
	public static void resetInstance() {
		ndd = null;
	}
}
