package com.crawljax.core.duplicatedetection;

import java.util.ArrayList;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.crawljax.core.state.duplicatedetection.FeatureType;
import com.crawljax.core.state.duplicatedetection.HashGenerator;
import com.crawljax.core.state.duplicatedetection.NearDuplicateDetectionCrawlHash32;
import com.crawljax.core.state.duplicatedetection.XxHashGeneratorFactory;

public class HammingDistanceTest {
	
	long time;
	
	@Test
	public void testHammingDistance() {
		HashGenerator hasher = new XxHashGeneratorFactory().getInstance();
		NearDuplicateDetectionCrawlHash32 crawlHesh = new NearDuplicateDetectionCrawlHash32(3, new ArrayList<FeatureType>(), hasher);
		String hash1 = "-1111111111111111111111111111111";
		String hash2 = "-1111111111101111111111111110111";
		
		int hash1AsInt = Integer.parseInt(hash1, 2);
		int hash2AsInt = Integer.parseInt(hash2, 2);
		
		int hammingDistance = crawlHesh.hammingDistance(hash1AsInt, hash2AsInt);
		
		assertEquals(2, hammingDistance);
	}
	
	@Test
	public void testHammingDistance2() {
		HashGenerator hasher = new XxHashGeneratorFactory().getInstance();
		NearDuplicateDetectionCrawlHash32 crawlHesh = new NearDuplicateDetectionCrawlHash32(3, new ArrayList<FeatureType>(), hasher);
		String hash1 = "01110111111111111011111110110111";
		String hash2 = "01111111111101111111110111110111";
		
		int hash1AsInt = Integer.parseInt(hash1, 2);
		int hash2AsInt = Integer.parseInt(hash2, 2);
		int hammingDistance = crawlHesh.hammingDistance(hash1AsInt, hash2AsInt);
		
		assertEquals(5, hammingDistance);
	}
	
	@Test
	public void testIsNearDuplicateOnBoundary() {
		HashGenerator hasher = new XxHashGeneratorFactory().getInstance();
		NearDuplicateDetectionCrawlHash32 crawlHesh = new NearDuplicateDetectionCrawlHash32(3, new ArrayList<FeatureType>(), hasher);
		String hash1 = "01111111111111111111111111111111";
		String hash2 = "01111111111101111111110111110111";
		
		int[] hash1AsInt = {Integer.parseInt(hash1, 2)};
		int[] hash2AsInt = {Integer.parseInt(hash2, 2)};
		
		boolean duplicate = crawlHesh.isNearDuplicateHash(hash1AsInt, hash2AsInt);
		
		assertEquals(3, crawlHesh.getThreshold(), 0.001);
		assertTrue(duplicate);
	}
	
	@Test
	public void testIsNearDuplicateOfBoundary() {
		HashGenerator hasher = new XxHashGeneratorFactory().getInstance();
		NearDuplicateDetectionCrawlHash32 crawlHesh = new NearDuplicateDetectionCrawlHash32(3, new ArrayList<FeatureType>(), hasher);
		String hash1 = "01111111111111111111111111111111";
		String hash2 = "01111111111101111111110111110111";
		
		int[] hash1AsInt = {Integer.parseInt(hash1, 2)};
		int[] hash2AsInt = {Integer.parseInt(hash2, 2)};
		
		boolean duplicate = crawlHesh.isNearDuplicateHash(hash1AsInt, hash2AsInt);
		
		assertEquals(2, crawlHesh.getThreshold(), 0.001);
		assertFalse(duplicate);
	}
	
	
}
