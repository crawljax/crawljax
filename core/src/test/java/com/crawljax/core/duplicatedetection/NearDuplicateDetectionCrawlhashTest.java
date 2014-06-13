package com.crawljax.core.duplicatedetection;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.crawljax.core.state.duplicatedetection.DuplicateDetectionException;
import com.crawljax.core.state.duplicatedetection.FeatureShingles;
import com.crawljax.core.state.duplicatedetection.FeatureException;
import com.crawljax.core.state.duplicatedetection.FeatureType;
import com.crawljax.core.state.duplicatedetection.Fingerprint;
import com.crawljax.core.state.duplicatedetection.HashGenerator;
import com.crawljax.core.state.duplicatedetection.NearDuplicateDetection;
import com.crawljax.core.state.duplicatedetection.NearDuplicateDetectionCrawlhash;
import com.crawljax.core.state.duplicatedetection.XxHashGenerator;

public class NearDuplicateDetectionCrawlhashTest {

	@Test
	public void testGetThreshold() throws FeatureException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(9, FeatureShingles.SizeType.WORDS));

		HashGenerator hasher = new XxHashGenerator();
		NearDuplicateDetectionCrawlhash ndd = new NearDuplicateDetectionCrawlhash(3, features, hasher);
		assertEquals(3, ndd.getDefaultThreshold(), 0.001);
	}
	
	@Test
	public void testFeatureSizeOnBoundary() throws FeatureException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(7, FeatureShingles.SizeType.WORDS));

		HashGenerator hasher = new XxHashGenerator();
		NearDuplicateDetectionCrawlhash ndd = new NearDuplicateDetectionCrawlhash(3, features, hasher);
		String strippedDom = "This is some text for the test.";
		ndd.generateFingerprint(strippedDom);
	}
	
	@Test (expected = FeatureException.class)
	public void testFeatureSizeOffBoundary() throws FeatureException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(8, FeatureShingles.SizeType.WORDS));

		HashGenerator hasher = new XxHashGenerator();
		NearDuplicateDetectionCrawlhash ndd = new NearDuplicateDetectionCrawlhash(3, features, hasher);
		String strippedDom = "This is some text for the test.";
		ndd.generateFingerprint(strippedDom);
	}
	
	@Test
	public void testSameDomToSameHash() throws FeatureException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.CHARS));

		HashGenerator hasher = new XxHashGenerator();
		NearDuplicateDetectionCrawlhash ndd = new NearDuplicateDetectionCrawlhash(3, features, hasher);
		String strippedDom1 = "Test";
		String strippedDom2 = "Test";
		
		Fingerprint fingerprint = ndd.generateFingerprint(strippedDom1);
		Fingerprint fingerprint2 = ndd.generateFingerprint(strippedDom2);
		assertTrue(fingerprint.isNearDuplicateHash(fingerprint2,0));
	}
	
	@Test
	public void testDifferendDomToDifferendHash() throws FeatureException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.WORDS));

		HashGenerator hasher = new XxHashGenerator();
		NearDuplicateDetectionCrawlhash ndd = new NearDuplicateDetectionCrawlhash(3, features, hasher);
		String strippedDom1 = "This is some text for the test.";
		String strippedDom2 = "Other text will be shown";
		
		Fingerprint fingerprint = ndd.generateFingerprint(strippedDom1);
		Fingerprint fingerprint2 = ndd.generateFingerprint(strippedDom2);
		assertThat(fingerprint.getDistance(fingerprint2), not(is(0)));
	}
	
	@Test (expected = DuplicateDetectionException.class)
	public void testMissingFeatures() {
		HashGenerator hasher = new XxHashGenerator();
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		new NearDuplicateDetectionCrawlhash(3, features, hasher);
	}
	
	@Test (expected = DuplicateDetectionException.class)
	public void testFeaturesIsNull() {
		HashGenerator hasher = new XxHashGenerator();
		new NearDuplicateDetectionCrawlhash(3, null, hasher);
	}
	
	@Test
	public void testThresholdOnBoundary() {
		HashGenerator hasher = new XxHashGenerator();
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.WORDS));
		new NearDuplicateDetectionCrawlhash(32, features, hasher);
	}
	
	@Test (expected = DuplicateDetectionException.class)
	public void testThresholdOffBoundary() {
		HashGenerator hasher = new XxHashGenerator();
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.WORDS));
		new NearDuplicateDetectionCrawlhash(33, features, hasher);
	}
	
	@Test (expected = DuplicateDetectionException.class)
	public void testToLowThreshold() {
		HashGenerator hasher = new XxHashGenerator();
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.WORDS));
		NearDuplicateDetection crawlhash = new NearDuplicateDetectionCrawlhash(1, features, hasher);
		crawlhash.setDefaultThreshold(-1);
	}
	
	@Test
	public void testSetThresholdCorrect() {
		HashGenerator hasher = new XxHashGenerator();
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.WORDS));
		NearDuplicateDetection crawlHash = new NearDuplicateDetectionCrawlhash(1, features, hasher);
		crawlHash.setDefaultThreshold(8);
		assertEquals(8, crawlHash.getDefaultThreshold(), 0.1);
	}
	
	@Test
	public void testSetFeaturesCorrect() {
		HashGenerator hasher = new XxHashGenerator();
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.WORDS));
		NearDuplicateDetection crawlHash = new NearDuplicateDetectionCrawlhash(1, features, hasher);
		
		List<FeatureType> newFeatures = new ArrayList<FeatureType>();
		newFeatures.add(new FeatureShingles(1, FeatureShingles.SizeType.CHARS));
		crawlHash.setFeatures(newFeatures);
		
		List<String> listOfFeatures = crawlHash.getFeatures().get(0).getFeatures("Test");
		assertEquals("T", listOfFeatures.get(0));
		assertEquals("e", listOfFeatures.get(1));
		assertEquals("s", listOfFeatures.get(2));
		assertEquals("t", listOfFeatures.get(3));
	}
}
