package com.crawljax.core.state.duplicatedetection;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import com.crawljax.core.state.duplicatedetection.BroderFingerprint;
import com.crawljax.core.state.duplicatedetection.Fingerprint;

public class BroderFingerprintTest {

	/**
	 * Test method for
	 * {@link com.crawljax.core.state.duplicatedetection.BroderFingerprint#getDistance(com.crawljax.core.state.duplicatedetection.Fingerprint)}
	 * .
	 */
	@Test
	public void testGetDistanceDifferentHashSize() {
		int[] set1 = { 1010, 1110, 1011, 0000 };
		int[] set2 = { 1011 };

		double distance = new BroderFingerprint(set1).getDistance(new BroderFingerprint(set2));
		assertEquals(3.0 / 4.0, distance, 0.001);
	}

	/**
	 * Tests if two sets of the same hash return 0 (= entirely equal)
	 */
	@Test
	public void testGetDistanceUniqueEqual() {
		int[] set1 = { 1111 };
		int[] set2 = { 1111, 1111, 1111 };

		double distance = new BroderFingerprint(set1).getDistance(new BroderFingerprint(set2));
		assertEquals(0, distance, 0.001);
	}

	/**
	 * Tests comparison when two sets are of equal size
	 */
	@Test
	public void testGetDistanceSameHashSize() {
		int[] set1 = { 1111, 0000, 1010, 0101 };
		int[] set2 = { 0001, 1111, 0101, 1110 };

		double distance = new BroderFingerprint(set1).getDistance(new BroderFingerprint(set2));
		assertEquals(4.0 / 6.0, distance, 0.001);
	}

	/**
	 * Test method for
	 * {@link com.crawljax.core.state.duplicatedetection.BroderFingerprint#isNearDuplicate(com.crawljax.core.state.duplicatedetection.Fingerprint)}
	 * .
	 */
	@Test
	public void testIsNearDuplicateHash1() {
		int[] set1 = { 1111, 0000, 1010, 0101 };
		int[] set2 = { 0101, 1111, 0000, 1010 };

		assertThat(new BroderFingerprint(set1).getDistance(new BroderFingerprint(set2)),
		        lessThan(0.8));
	}

	/**
	 * Tests isNearDuplicateHash/2 for a case that hashes are near-duplicate
	 */
	@Test
	public void testIsNearDuplicateHashFingerprintDoubleOnBoundary() {

		int[] set1 = { 1111, 0000, 1010, 0101, 1110 };
		int[] set2 = { 0101, 1111, 0000, 1010, 0001 };

		double distance = new BroderFingerprint(set1).getDistance(new BroderFingerprint(set2));
		assertThat(distance, lessThan(2.0 / 6.0 + 0.00001));

		boolean duplicate =
		        new BroderFingerprint(set1).isNearDuplicate(new BroderFingerprint(set2), 0.8);
		assertTrue(duplicate);
	}

	/**
	 * Tests isNearDuplicateHash/2 for a case that hashes are not near-duplicate
	 */
	@Test
	public void testIsNearDuplicateHashFingerprintDoubleOffBoundary() {
		int[] set1 = { 1111, 0000, 1010, 0101, 1110 };
		int[] set2 = { 0101, 1111, 0000, 1010, 0001 };

		double distance = new BroderFingerprint(set1).getDistance(new BroderFingerprint(set2));
		assertThat(distance, greaterThan(2.0 / 6.0));

		boolean duplicate =
		        new BroderFingerprint(set1).isNearDuplicate(new BroderFingerprint(set2),
		                2.0 / 6.0);
		assertFalse(duplicate);
	}

	/**
	 * Tests isNearDuplicateHash/2 for a case that hashes are near-duplicate
	 */
	@Test
	public void testIsNearDuplicateHashFingerprintOnBoundary() {

		int[] set1 = { 1111, 0000, 1010, 0101, 1110 };
		int[] set2 = { 0101, 1111, 0000, 1010, 0001 };

		double distance = new BroderFingerprint(set1).getDistance(new BroderFingerprint(set2));
		assertThat(distance, lessThan(2.0 / 6.0 + 0.00001));

		boolean duplicate =
		        new BroderFingerprint(set1, 0.8).isNearDuplicate(new BroderFingerprint(set2));
		assertTrue(duplicate);
	}

	/**
	 * Tests isNearDuplicateHash/2 for a case that hashes are not near-duplicate
	 */
	@Test
	public void testIsNearDuplicateHashFingerprintOffBoundary() {
		int[] set1 = { 1111, 0000, 1010, 0101, 1110 };
		int[] set2 = { 0101, 1111, 0000, 1010, 0001 };

		double distance = new BroderFingerprint(set1).getDistance(new BroderFingerprint(set2));
		assertThat(distance, greaterThan(2.0 / 6.0));

		boolean duplicate =
		        new BroderFingerprint(set1, 2.0 / 6.0).isNearDuplicate(new BroderFingerprint(
		                set2));
		assertFalse(duplicate);
	}
	
	/**
	 * Tests isNearDuplicateHash/2 for a case that hashes are not near-duplicate
	 */
	@Test
	public void testIsNearDuplicateHashFingerprintZero() {
		int[] set1 = { 1111 };
		int[] set2 = { 1111 };

		double distance = new BroderFingerprint(set1).getDistance(new BroderFingerprint(set2));
		assertThat(distance, is(0.0));

		boolean duplicate =
		        new BroderFingerprint(set1, 0).isNearDuplicate(new BroderFingerprint(
		                set2));
		assertThat(duplicate, is(true));
	}

	@Test(expected = RuntimeException.class)
	public void testGetDistanceOtherObjectType() {
		int[] set1 = { 1111, 0000, 1010, 0101, 1110 };
		BroderFingerprint fingerprint = new BroderFingerprint(set1);
		fingerprint.getDistance(mock(Fingerprint.class));
	}

	@Test(expected = RuntimeException.class)
	public void testGetDistanceNull() {
		int[] set1 = { 1111, 0000, 1010, 0101, 1110 };
		BroderFingerprint fingerprint = new BroderFingerprint(set1);
		fingerprint.getDistance(null);
	}

	@Test
	public void testEqualsNull() {
		int[] set1 = { 1111, 0000, 1010, 0101, 1110 };
		BroderFingerprint hash1 = new BroderFingerprint(set1);
		assertFalse(hash1.equals(null));
	}

	@Test
	public void testEqualsOtherObject() {
		int[] set1 = { 1111, 0000, 1010, 0101, 1110 };
		BroderFingerprint hash1 = new BroderFingerprint(set1);
		assertFalse(hash1.equals(new Object()));
	}

	@Test
	public void testEqualsSame() {
		int[] set1 = { 1111, 0000, 1010, 0101, 1110 };
		BroderFingerprint hash1 = new BroderFingerprint(set1);
		assertTrue(hash1.equals(hash1));
	}

	@Test
	public void testEqualsSameHash() {
		int[] set1 = { 1111, 0000, 1010, 0101, 1110 };
		BroderFingerprint hash1 = new BroderFingerprint(set1);
		BroderFingerprint hash2 = new BroderFingerprint(set1);
		assertTrue(hash1.equals(hash2));
	}

	@Test
	public void testEqualsOtherHash() {
		int[] set1 = { 1111, 0000 };
		int[] set2 = { 1111, 1110 };
		BroderFingerprint hash1 = new BroderFingerprint(set1);
		BroderFingerprint hash2 = new BroderFingerprint(set2);
		assertFalse(hash1.equals(hash2));
	}
	
	@Test(expected=DuplicateDetectionException.class)
	public void testInvalidThresholdException() {
		int[] set1 = { 1111, 0000 };
		new BroderFingerprint(set1,42);
	}
	
	@Test(expected=DuplicateDetectionException.class)
	public void testInvalidThresholdException2() {
		int[] set1 = { 1111, 0000 };
		new BroderFingerprint(set1,-1);
	}
	
	@Test
	public void testCrawlhashFingerprint() {
		final double threshold = 0.2;
		int[] set1 = { 1111, 0000 };
		Fingerprint fingerprint = new BroderFingerprint(set1, threshold);
		assertNotNull(fingerprint.toString());
		assertEquals(fingerprint.getDefaultThreshold(),threshold,0.01);
		assertEquals(fingerprint.getThresholdLowerlimit(), 0, 0.01);
		assertEquals(fingerprint.getThresholdUpperlimit(), 1, 0.01);
	}
	
}
