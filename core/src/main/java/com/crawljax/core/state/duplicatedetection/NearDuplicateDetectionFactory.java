package com.crawljax.core.state.duplicatedetection;

public class NearDuplicateDetectionFactory {
	private static NearDuplicateDetectionCrawlHash ndd;
	
	public static NearDuplicateDetectionCrawlHash getInstance() {
		if (ndd == null) {
			ndd = new NearDuplicateDetectionCrawlHash();
		}
		return ndd;
	}
}
