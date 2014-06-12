package com.crawljax.core.duplicatedetection;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.crawljax.core.state.duplicatedetection.*;

public class NearDuplicateDetectionBroderTest {
	
	private List<FeatureType> features = new ArrayList<FeatureType>();
	
	
	@Test
	public void testGenerateHash() throws FeatureException {
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.WORDS));
		HashGenerator hasher = new XxHashGenerator();
		NearDuplicateDetectionBroder broder = new NearDuplicateDetectionBroder(0.8, features, hasher);
		
		String testDoc = "This will be a test";
		int[] hashes = broder.generateHash(testDoc).getHashesAsIntArray();
		assertEquals(hasher.generateHash("Thiswill"), hashes[0]);
		assertEquals(hasher.generateHash("willbe"), hashes[1]);
		assertEquals(hasher.generateHash("bea"), hashes[2]);
		assertEquals(hasher.generateHash("atest"), hashes[3]);
	}
	
	@Test (expected = DuplicateDetectionException.class)
	public void testMissingFeatures() {
		HashGenerator hasher = new XxHashGenerator();
		new NearDuplicateDetectionBroder(2.0/6.0, features, hasher);
	}
	
	@Test (expected = DuplicateDetectionException.class)
	public void testFeaturesIsNull() {
		HashGenerator hasher = new XxHashGenerator();
		new NearDuplicateDetectionBroder(2.0/6.0, null, hasher);
	}
	
	@Test (expected = DuplicateDetectionException.class)
	public void testToHighThreshold() {
		HashGenerator hasher = new XxHashGenerator();
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.WORDS));
		new NearDuplicateDetectionBroder(5, features, hasher);
	}
	
	@Test (expected = DuplicateDetectionException.class)
	public void testToLowThreshold() {
		HashGenerator hasher = new XxHashGenerator();
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.WORDS));
		NearDuplicateDetection broder = new NearDuplicateDetectionBroder(1, features, hasher);
		broder.setThreshold(-1);
	}
	
	@Test
	public void testSetThresholdCorrect() {
		HashGenerator hasher = new XxHashGenerator();
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.WORDS));
		NearDuplicateDetection broder = new NearDuplicateDetectionBroder(1, features, hasher);
		broder.setThreshold(0.3);
		assertEquals(0.3, broder.getThreshold(), 0.0001);
	}
	
	@Test
	public void testSetFeaturesCorrect() {
		HashGenerator hasher = new XxHashGenerator();
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.WORDS));
		NearDuplicateDetection broder = new NearDuplicateDetectionBroder(1, features, hasher);
		
		List<FeatureType> newFeatures = new ArrayList<FeatureType>();
		newFeatures.add(new FeatureShingles(1, FeatureShingles.SizeType.CHARS));
		broder.setFeatures(newFeatures);
		
		List<String> listOfFeatures = broder.getFeatures().get(0).getFeatures("Test");
		assertEquals("T", listOfFeatures.get(0));
		assertEquals("e", listOfFeatures.get(1));
		assertEquals("s", listOfFeatures.get(2));
		assertEquals("t", listOfFeatures.get(3));
	}
}
