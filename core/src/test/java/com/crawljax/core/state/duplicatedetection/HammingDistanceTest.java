package com.crawljax.core.state.duplicatedetection;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.crawljax.core.state.duplicatedetection.CrawlhashFingerprint;
import com.crawljax.core.state.duplicatedetection.FeatureShingles;
import com.crawljax.core.state.duplicatedetection.FeatureShingles.SizeType;
import com.crawljax.core.state.duplicatedetection.FeatureType;
import com.crawljax.core.state.duplicatedetection.Fingerprint;
import com.crawljax.core.state.duplicatedetection.HashGenerator;
import com.crawljax.core.state.duplicatedetection.NearDuplicateDetectionCrawlhash;
import com.crawljax.core.state.duplicatedetection.XxHashGenerator;
import com.google.common.collect.ImmutableList;

public class HammingDistanceTest {

	long time;

	@Test
	public void testHammingDistance() {
		String hash1 = "-1111111111111111111111111111111";
		String hash2 = "-1111111111101111111111111110111";

		int hash1AsInt = Integer.parseInt(hash1, 2);
		int hash2AsInt = Integer.parseInt(hash2, 2);

		int hammingDistance =
		        new CrawlhashFingerprint(0, 0).hammingDistance(hash1AsInt, hash2AsInt);

		assertEquals(2, hammingDistance);
	}

	@Test
	public void testHammingDistance2() {
		String hash1 = "01110111111111111011111110110111";
		String hash2 = "01111111111101111111110111110111";

		int hash1AsInt = Integer.parseInt(hash1, 2);
		int hash2AsInt = Integer.parseInt(hash2, 2);
		int hammingDistance =
		        new CrawlhashFingerprint(0, 0).hammingDistance(hash1AsInt, hash2AsInt);

		assertEquals(5, hammingDistance);
	}

	@Test
	public void testIsNearDuplicateOnBoundary() {
		HashGenerator hasher = new XxHashGenerator();
		List<FeatureType> features = new ArrayList<>();
		features.add(new FeatureShingles(2, SizeType.CHARS));
		NearDuplicateDetectionCrawlhash crawlhash =
		        new NearDuplicateDetectionCrawlhash(3, ImmutableList.copyOf(features), hasher);
		String hash = "01111111111111111111111111111111";
		String threeBitsDiffHash = "01111111111101111111110111110111";

		int hash1AsInt = Integer.parseInt(hash, 2);
		int hash2AsInt = Integer.parseInt(threeBitsDiffHash, 2);

		Fingerprint fpHash =
		        new CrawlhashFingerprint(hash1AsInt, crawlhash.getDefaultThreshold());
		Fingerprint fpThreeBitsDIffHash =
		        new CrawlhashFingerprint(hash2AsInt, crawlhash.getDefaultThreshold());

		double distance = fpHash.getDistance(fpThreeBitsDIffHash);
		assertEquals(distance, 3, 0.01);

		boolean duplicate = fpHash.isNearDuplicate(new CrawlhashFingerprint(hash2AsInt, 3));
		assertEquals(3, crawlhash.getDefaultThreshold(), 0.001);
		assertTrue(duplicate);
	}

	@Test
	public void testIsNearDuplicateOfBoundary() {
		HashGenerator hasher = new XxHashGenerator();
		List<FeatureType> features = new ArrayList<>();
		features.add(new FeatureShingles(2, SizeType.CHARS));
		NearDuplicateDetectionCrawlhash crawlhash =
		        new NearDuplicateDetectionCrawlhash(2, ImmutableList.copyOf(features), hasher);
		String hash = "01111111111111111111111111111111";
		String threeBitsDiffhash = "01111111111101111111110111110111";

		int hash1AsInt = Integer.parseInt(hash, (int) crawlhash.getDefaultThreshold());
		int hash2AsInt =
		        Integer.parseInt(threeBitsDiffhash, (int) crawlhash.getDefaultThreshold());

		Fingerprint fpHash =
		        new CrawlhashFingerprint(hash1AsInt, crawlhash.getDefaultThreshold());
		Fingerprint fpThreeBitsDIffHash =
		        new CrawlhashFingerprint(hash2AsInt, crawlhash.getDefaultThreshold());

		double distance = fpHash.getDistance(fpThreeBitsDIffHash);
		assertEquals(distance, 3, 0.01);

		boolean duplicate =
		        new CrawlhashFingerprint(hash1AsInt, 2)
		                .isNearDuplicate(new CrawlhashFingerprint(hash2AsInt, 2));
		assertEquals(2, crawlhash.getDefaultThreshold(), 0.001);
		assertFalse(duplicate);
	}

}
