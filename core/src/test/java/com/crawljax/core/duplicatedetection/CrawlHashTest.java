package com.crawljax.core.duplicatedetection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import com.crawljax.core.state.duplicatedetection.FeatureShingles;
import com.crawljax.core.state.duplicatedetection.FeatureException;
import com.crawljax.core.state.duplicatedetection.FeatureType;
import com.crawljax.core.state.duplicatedetection.HashGenerator;
import com.crawljax.core.state.duplicatedetection.NearDuplicateDetectionCrawlHash32;
import com.crawljax.core.state.duplicatedetection.XxHashGenerator;

public class CrawlHashTest {

	@Test
	public void testGetThreshold() throws FeatureException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(9, FeatureShingles.SizeType.WORDS));

		HashGenerator hasher = new XxHashGenerator();
		NearDuplicateDetectionCrawlHash32 ndd = new NearDuplicateDetectionCrawlHash32(3, features, hasher);
		assertEquals(3, ndd.getThreshold(), 0.001);
	}
	
	@Test
	public void testDuplicateOnSameState() throws FeatureException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(3, FeatureShingles.SizeType.WORDS));

		HashGenerator hasher = new XxHashGenerator();
		NearDuplicateDetectionCrawlHash32 ndd = new NearDuplicateDetectionCrawlHash32(3, features, hasher);
		String strippedDom = "This is some text for the test.";
		int[] hash = ndd.generateHash(strippedDom);
		boolean duplicate = ndd.isNearDuplicateHash(hash, hash);
		assertTrue(duplicate);
		
		double distance = ndd.getDistance(hash, hash);
		assertEquals(0, distance, 0.001);
	}
	
	@Test
	public void testDuplicateOnNewState() throws FeatureException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(3, FeatureShingles.SizeType.WORDS));
		HashGenerator hasher = new XxHashGenerator();
		NearDuplicateDetectionCrawlHash32 ndd = new NearDuplicateDetectionCrawlHash32(3, features, hasher);
		String strippedDom1 = "This is some text for the test.";
		String strippedDom2 = "This is some text for the test.";
		boolean duplicate = ndd.isNearDuplicateHash(ndd.generateHash(strippedDom1),ndd.generateHash(strippedDom2));
		assertTrue(duplicate);
	}
	
	@Test
	public void testNotDuplicate() throws FeatureException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(3, FeatureShingles.SizeType.WORDS));

		HashGenerator hasher = new XxHashGenerator();
		NearDuplicateDetectionCrawlHash32 ndd = new NearDuplicateDetectionCrawlHash32(3, features, hasher);
		String strippedDom1 = "This is some text for the test.";
		String strippedDom2 = "Whole other test goes in here.";
		boolean duplicate = ndd.isNearDuplicateHash(ndd.generateHash(strippedDom1),ndd.generateHash(strippedDom2));
		assertFalse(duplicate);
	}
	
	@Test
	public void testFeatureSizeOnBoundary() throws FeatureException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(7, FeatureShingles.SizeType.WORDS));

		HashGenerator hasher = new XxHashGenerator();
		NearDuplicateDetectionCrawlHash32 ndd = new NearDuplicateDetectionCrawlHash32(3, features, hasher);
		String strippedDom = "This is some text for the test.";
		ndd.generateHash(strippedDom);
	}
	
	@Test (expected = FeatureException.class)
	public void testFeatureSizeOffBoundary() throws FeatureException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(8, FeatureShingles.SizeType.WORDS));

		HashGenerator hasher = new XxHashGenerator();
		NearDuplicateDetectionCrawlHash32 ndd = new NearDuplicateDetectionCrawlHash32(3, features, hasher);
		String strippedDom = "This is some text for the test.";
		ndd.generateHash(strippedDom);
	}
	
	@Test
	public void testSameDomToSameHash() throws FeatureException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.CHARS));

		HashGenerator hasher = new XxHashGenerator();
		NearDuplicateDetectionCrawlHash32 ndd = new NearDuplicateDetectionCrawlHash32(3, features, hasher);
		String strippedDom1 = "Test";
		String strippedDom2 = "Test";
		
		int[] hash1 = ndd.generateHash(strippedDom1);
		int[] hash2 = ndd.generateHash(strippedDom2);
		assertEquals(hash1[0], hash2[0]);
	}
	
	@Test
	public void testDifferendDomToDifferendHash() throws FeatureException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.WORDS));

		HashGenerator hasher = new XxHashGenerator();
		NearDuplicateDetectionCrawlHash32 ndd = new NearDuplicateDetectionCrawlHash32(3, features, hasher);
		String strippedDom1 = "This is some text for the test.";
		String strippedDom2 = "Other text will be shown";
		
		int[] hashOfvFromDom = ndd.generateHash(strippedDom1);
		int[] hashOfwFromDom = ndd.generateHash(strippedDom2);
		assertFalse(hashOfvFromDom[0] == hashOfwFromDom[0]);
	}
	
	@Test (expected = AssertionError.class)
	public void testDomIsNull() throws FeatureException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.WORDS));

		HashGenerator hasher = new XxHashGenerator();
		NearDuplicateDetectionCrawlHash32 ndd = new NearDuplicateDetectionCrawlHash32(3, features, hasher);
		String dom = null;
		ndd.generateHash(dom);
	}
	
	@Test (expected = AssertionError.class)
	public void testStrippedDomIsNull() throws FeatureException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.WORDS));

		HashGenerator hasher = new XxHashGenerator();
		NearDuplicateDetectionCrawlHash32 ndd = new NearDuplicateDetectionCrawlHash32(3, features, hasher);
		String strippedDom = null;
		ndd.generateHash(strippedDom);
	}
}
