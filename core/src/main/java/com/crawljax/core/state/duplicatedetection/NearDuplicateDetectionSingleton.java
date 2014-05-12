package com.crawljax.core.state.duplicatedetection;

public class NearDuplicateDetectionSingleton {
	private static NearDuplicateDetection ndd;
	
	public static NearDuplicateDetection getInstance() {
		if (ndd == null) {
			ndd = new NearDuplicateDetectionCrawlHash32(4);
		}
		return ndd;
	}
}
