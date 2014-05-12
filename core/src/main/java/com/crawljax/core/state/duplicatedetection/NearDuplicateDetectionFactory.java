package com.crawljax.core.state.duplicatedetection;

public class NearDuplicateDetectionFactory {
	private static NearDuplicateDetection ndd;
	
	public static NearDuplicateDetection getInstance() {
		if (ndd == null) {
			ndd = new NearDuplicateDetectionCrawlHash32();
		}
		return ndd;
	}
}
