package com.crawljax.core.duplicatedetection;

import java.util.ArrayList;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.crawljax.core.state.duplicatedetection.FeatureType;
import com.crawljax.core.state.duplicatedetection.NearDuplicateDetectionCrawlHash32;

public class HammingDistanceTest {
	
	long time;
	
	@Test
	public void testHammingDistance() {
		NearDuplicateDetectionCrawlHash32 crawlHesh = new NearDuplicateDetectionCrawlHash32(3, new ArrayList<FeatureType>());
		String hash1 = "11111111111111111111111111111111";
		String hash2 = "11111111111101111111110111110111";
		
		int hash1AsInt = (int) Long.parseLong(hash1, 2);
		int hash2AsInt = (int) Long.parseLong(hash2, 2);
		
		int hammingDistance = crawlHesh.hammingDistance(hash1AsInt, hash2AsInt);
		
		assertEquals(3, hammingDistance);
	}
	
	@Test
	public void testHammingDistance2() {
		NearDuplicateDetectionCrawlHash32 crawlHesh = new NearDuplicateDetectionCrawlHash32(3, new ArrayList<FeatureType>());
		String hash1 = "11110111111111111011111110110111";
		String hash2 = "11111111111101111111110111110111";
		
		int hash1AsInt = (int) Long.parseLong(hash1, 2);
		int hash2AsInt = (int) Long.parseLong(hash2, 2);
		int hammingDistance = crawlHesh.hammingDistance(hash1AsInt, hash2AsInt);
		
		assertEquals(5, hammingDistance);
	}
	
	@Test
	public void testIsNearDuplicateOnBoundary() {
		NearDuplicateDetectionCrawlHash32 crawlHesh = new NearDuplicateDetectionCrawlHash32(3, new ArrayList<FeatureType>());
		String hash1 = "11111111111111111111111111111111";
		String hash2 = "11111111111101111111110111110111";
		
		long hash1AsInt = Long.parseLong(hash1, 2);
		long hash2AsInt = Long.parseLong(hash2, 2);
		
		boolean duplicate = crawlHesh.isNearDuplicateHash(hash1AsInt, hash2AsInt);
		
		assertEquals(3, crawlHesh.getThreshold());
		assertTrue(duplicate);
	}
	
	@Test
	public void testIsNearDuplicateOfBoundary() {
		NearDuplicateDetectionCrawlHash32 crawlHesh = new NearDuplicateDetectionCrawlHash32(2, new ArrayList<FeatureType>());
		String hash1 = "11111111111111111111111111111111";
		String hash2 = "11111111111101111111110111110111";
		
		long hash1AsInt = Long.parseLong(hash1, 2);
		long hash2AsInt = Long.parseLong(hash2, 2);
		
		boolean duplicate = crawlHesh.isNearDuplicateHash(hash1AsInt, hash2AsInt);
		
		assertEquals(2, crawlHesh.getThreshold());
		assertFalse(duplicate);
	}
	
	
}
