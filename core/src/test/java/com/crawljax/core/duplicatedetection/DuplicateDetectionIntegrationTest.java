package com.crawljax.core.duplicatedetection;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.crawljax.core.state.duplicatedetection.*;

public class DuplicateDetectionIntegrationTest {

	/**
	 * Tests the generation and comparison of Broder-fingerprints
	 */
	@Test
	public void BroderGenerateAndCompare() {
		// Setup
		List<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(1, FeatureShingles.SizeType.WORDS));
		HashGenerator hashGenerator = new XxHashGenerator();
		NearDuplicateDetection ndd = new NearDuplicateDetectionBroder(0.2, features, hashGenerator);
		
		// Generate fingerprints
		Fingerprint fingerprint1 = ndd.generateFingerprint("Chuck Norris writes code that optimizes itself.");
		Fingerprint fingerprint2 = ndd.generateFingerprint("Chuck Norris orders code to optimize itself.");
		
		// Compare fingerprints
		assertTrue(fingerprint1.isNearDuplicateHash(fingerprint1, 0));
		assertFalse(fingerprint1.isNearDuplicateHash(fingerprint2, 0));
		assertEquals(fingerprint1.getDistance(fingerprint2), fingerprint2.getDistance(fingerprint1), 0.01);
	}
	
	/**
	 * Tests the generation and comparison of Crawlhash-fingerprints
	 */
	@Test
	public void CrawlhashGenerateAndCompare() {
		// Setup
		List<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(1, FeatureShingles.SizeType.WORDS));
		HashGenerator hashGenerator = new XxHashGenerator();
		NearDuplicateDetection ndd = new NearDuplicateDetectionCrawlhash(2, features, hashGenerator);
		
		// Generate fingerprints
		Fingerprint fingerprint1 = ndd.generateFingerprint("Chuck Norris writes code that optimizes itself.");
		Fingerprint fingerprint2 = ndd.generateFingerprint("Chuck Norris orders code to optimize itself.");
		
		// Compare fingerprints
		assertTrue(fingerprint1.isNearDuplicateHash(fingerprint1, 0));
		assertFalse(fingerprint1.isNearDuplicateHash(fingerprint2, 0));
		assertEquals(fingerprint1.getDistance(fingerprint2), fingerprint2.getDistance(fingerprint1), 0.01);
	}
}
