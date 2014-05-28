package com.crawljax.core.duplicatedetection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.crawljax.core.state.duplicatedetection.FeatureShingles;
import com.crawljax.core.state.duplicatedetection.FeatureException;
import com.crawljax.core.state.duplicatedetection.FeatureType;
import com.crawljax.core.state.duplicatedetection.NearDuplicateDetectionBroder32;

public class BroderTest {
	
	private List<FeatureType> features = new ArrayList<FeatureType>();
	
	@Test
	public void testGenerateHash() throws FeatureException {
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.WORDS));
		NearDuplicateDetectionBroder32 broder = new NearDuplicateDetectionBroder32(0.8, features);
		
		String testDoc = "This will be a test";
		int[] hashes = broder.generateHash(testDoc);
		assertEquals(new String("Thiswill").hashCode(), hashes[0]);
		assertEquals(new String("willbe").hashCode(), hashes[1]);
		assertEquals(new String("bea").hashCode(), hashes[2]);
		assertEquals(new String("atest").hashCode(), hashes[3]);
	}
	
	@Test
	public void testGetDistance1() throws FeatureException {
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.WORDS));
		NearDuplicateDetectionBroder32 broder = new NearDuplicateDetectionBroder32(0.8, features);
		
		int[] set1 = {1010, 1110, 1011, 0000};
		int[] set2 = {1011};
		
		double distance = broder.getDistance(set1, set2);
		assertEquals(1.0/4.0, distance, 0.001);
	}
	
	@Test
	public void testGetDistance2() throws FeatureException {
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.WORDS));
		NearDuplicateDetectionBroder32 broder = new NearDuplicateDetectionBroder32(0.8, features);
		
		int[] set1 = {1111};
		int[] set2 = {1111, 1111, 1111};
		
		double distance = broder.getDistance(set1, set2);
		assertEquals(1, distance, 0.001);
	}
	
	@Test
	public void testGetDistance3() throws FeatureException {
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.WORDS));
		NearDuplicateDetectionBroder32 broder = new NearDuplicateDetectionBroder32(0.8, features);
		
		int[] set1 = {1111, 0000, 1010, 0101};
		int[] set2 = {0001, 1111, 0101, 1110};
		
		double distance = broder.getDistance(set1, set2);
		assertEquals(2.0/6.0, distance, 0.001);
	}
	
	@Test
	public void testIsNearDuplicateHash1() throws FeatureException {
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.WORDS));
		NearDuplicateDetectionBroder32 broder = new NearDuplicateDetectionBroder32(0.8, features);
		
		int[] set1 = {1111, 0000, 1010, 0101};
		int[] set2 = {0101, 1111, 0000, 1010};
		
		boolean duplicate = broder.isNearDuplicateHash(set1, set2);
		assertTrue(duplicate);
	}
	
	@Test
	public void testIsNearDuplicateHashOnBoundary() throws FeatureException {
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.WORDS));
		NearDuplicateDetectionBroder32 broder = new NearDuplicateDetectionBroder32(4.0/6.0, features);
		
		int[] set1 = {1111, 0000, 1010, 0101, 1110};
		int[] set2 = {0101, 1111, 0000, 1010, 0001};
		
		boolean duplicate = broder.isNearDuplicateHash(set1, set2);
		assertTrue(duplicate);
	}
	
	@Test
	public void testIsNearDuplicateHashOffBoundary() throws FeatureException {
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.WORDS));
		NearDuplicateDetectionBroder32 broder = new NearDuplicateDetectionBroder32(4.0/6.0 + 0.001, features);
		
		int[] set1 = {1111, 0000, 1010, 0101, 1110};
		int[] set2 = {0101, 1111, 0000, 1010, 0001};
		
		boolean duplicate = broder.isNearDuplicateHash(set1, set2);
		assertFalse(duplicate);
	}
}
