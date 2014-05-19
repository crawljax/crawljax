package com.crawljax.core.duplicatedetection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import com.crawljax.core.state.duplicatedetection.FeatureShingles;
import com.crawljax.core.state.duplicatedetection.FeatureShinglesException;
import com.crawljax.core.state.duplicatedetection.FeatureType;
import com.crawljax.core.state.duplicatedetection.NearDuplicateDetectionCrawlHash32;
import com.crawljax.core.state.duplicatedetection.FeatureSizeType;

public class CrawlHashTest {

	@Test
	public void testGetThreshold() throws FeatureShinglesException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(9, FeatureSizeType.WORDS));
		
		NearDuplicateDetectionCrawlHash32 ndd = new NearDuplicateDetectionCrawlHash32(3, features);
		assertEquals(3, ndd.getThreshold());
	}
	
	@Test
	public void testDuplicateOnSameState() throws FeatureShinglesException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(3, FeatureSizeType.WORDS));
		
		NearDuplicateDetectionCrawlHash32 ndd = new NearDuplicateDetectionCrawlHash32(3, features);
		String strippedDom = "This is some text for the test.";
		int hash = ndd.generateHash(strippedDom);
		boolean duplicate = ndd.isNearDuplicateHash(hash, hash);
		assertTrue(duplicate);
		
		int distance = ndd.getDistance(hash, hash);
		assertEquals(0, distance);
	}
	
	@Test
	public void testDuplicateOnNewState() throws FeatureShinglesException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(3, FeatureSizeType.WORDS));
		
		NearDuplicateDetectionCrawlHash32 ndd = new NearDuplicateDetectionCrawlHash32(3, features);
		String strippedDom1 = "This is some text for the test.";
		String strippedDom2 = "This is some text for the test.";
		boolean duplicate = ndd.isNearDuplicateHash(ndd.generateHash(strippedDom1),ndd.generateHash(strippedDom2));
		assertTrue(duplicate);
	}
	
	@Test
	public void testNotDuplicate() throws FeatureShinglesException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(3, FeatureSizeType.WORDS));
		
		NearDuplicateDetectionCrawlHash32 ndd = new NearDuplicateDetectionCrawlHash32(3, features);
		String strippedDom1 = "This is some text for the test.";
		String strippedDom2 = "Whole other test goes in here.";
		boolean duplicate = ndd.isNearDuplicateHash(ndd.generateHash(strippedDom1),ndd.generateHash(strippedDom2));
		assertFalse(duplicate);
	}
	
	@Test
	public void testFeatureSizeOnBoundary() throws FeatureShinglesException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(7, FeatureSizeType.WORDS));
		
		NearDuplicateDetectionCrawlHash32 ndd = new NearDuplicateDetectionCrawlHash32(3, features);
		String strippedDom = "This is some text for the test.";
		ndd.generateHash(strippedDom);
	}
	
	@Test (expected = FeatureShinglesException.class)
	public void testFeatureSizeOffBoundary() throws FeatureShinglesException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(8, FeatureSizeType.WORDS));
		
		NearDuplicateDetectionCrawlHash32 ndd = new NearDuplicateDetectionCrawlHash32(3, features);
		String strippedDom = "This is some text for the test.";
		ndd.generateHash(strippedDom);
	}
	
	@Test
	public void testSameDomToSameHash() throws FeatureShinglesException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(2, FeatureSizeType.CHARS));
		
		NearDuplicateDetectionCrawlHash32 ndd = new NearDuplicateDetectionCrawlHash32(3, features);
		String strippedDom1 = "Test";
		String strippedDom2 = "Test";
		
		int hash1 = ndd.generateHash(strippedDom1);
		int hash2 = ndd.generateHash(strippedDom2);
		assertEquals(hash1, hash2);
	}
	
	@Test
	public void testDifferendDomToDifferendHash() throws FeatureShinglesException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(2, FeatureSizeType.WORDS));
		
		NearDuplicateDetectionCrawlHash32 ndd = new NearDuplicateDetectionCrawlHash32(3, features);
		String strippedDom1 = "This is some text for the test.";
		String strippedDom2 = "Other text will be shown";
		
		int hashOfvFromDom = ndd.generateHash(strippedDom1);
		int hashOfwFromDom = ndd.generateHash(strippedDom2);
		assertFalse(hashOfvFromDom == hashOfwFromDom);
	}
	
	@Test (expected = AssertionError.class)
	public void testDomIsNull() throws FeatureShinglesException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(2, FeatureSizeType.WORDS));
		
		NearDuplicateDetectionCrawlHash32 ndd = new NearDuplicateDetectionCrawlHash32(3, features);
		String dom = null;
		ndd.generateHash(dom);
	}
	
	@Test (expected = AssertionError.class)
	public void testStrippedDomIsNull() throws FeatureShinglesException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(2, FeatureSizeType.WORDS));
		
		NearDuplicateDetectionCrawlHash32 ndd = new NearDuplicateDetectionCrawlHash32(3, features);
		String strippedDom = null;
		ndd.generateHash(strippedDom);
	}
}
