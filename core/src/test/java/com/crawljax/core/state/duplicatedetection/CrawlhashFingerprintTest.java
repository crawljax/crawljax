/**
 * 
 */
package com.crawljax.core.state.duplicatedetection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import com.crawljax.core.state.duplicatedetection.CrawlhashFingerprint;
import com.crawljax.core.state.duplicatedetection.Fingerprint;

/**
 * Tests for Crawlhash-fingerprint,
 */
public class CrawlhashFingerprintTest {

	/**
	 * Test method for
	 * {@link com.crawljax.core.state.duplicatedetection.CrawlhashFingerprint#isNearDuplicate(com.crawljax.core.state.duplicatedetection.Fingerprint)}
	 * .
	 */
	@Test
	public void testIsNearDuplicateHashFingerprint() {
		Fingerprint hash = new CrawlhashFingerprint(Integer.parseInt("1001", 2), 2);
		Fingerprint oneBitsDiffHash = new CrawlhashFingerprint(Integer.parseInt("1000", 2));

		double distance = hash.getDistance(oneBitsDiffHash);
		assertThat(distance, is(1.0));

		assertTrue(hash.isNearDuplicate(oneBitsDiffHash));
	}

	@Test
	public void testIsNearDuplicateHashFingerprintNot() {
		Fingerprint hash = new CrawlhashFingerprint(Integer.parseInt("1111", 2), 2);
		Fingerprint threeBitsDiffHash = new CrawlhashFingerprint(Integer.parseInt("1000", 2));

		double distance = hash.getDistance(threeBitsDiffHash);
		assertThat(distance, is(3.0));

		assertFalse(hash.isNearDuplicate(threeBitsDiffHash));
	}

	@Test
	public void testIsNearDuplicateHashFingerprintEqual() {
		Fingerprint hash = new CrawlhashFingerprint(Integer.parseInt("1000", 2), 0);
		Fingerprint hashSame = new CrawlhashFingerprint(Integer.parseInt("1000", 2));

		double distance = hash.getDistance(hashSame);
		assertThat(distance, is(0.0));

		assertTrue(hash.isNearDuplicate(hashSame));
	}

	@Test
	public void testIsNearDuplicateHashFingerprintReverse() {
		Fingerprint hash1 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		Fingerprint hash2 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		assertEquals(hash1.isNearDuplicate(hash2, 0), hash2.isNearDuplicate(hash1, 0));
	}

	/**
	 * Test method for
	 * {@link com.crawljax.core.state.duplicatedetection.CrawlhashFingerprint#isNearDuplicate(com.crawljax.core.state.duplicatedetection.Fingerprint, double)}
	 * .
	 */
	@Test
	public void testIsNearDuplicateHashFingerprintDouble() {
		Fingerprint hash = new CrawlhashFingerprint(Integer.parseInt("1001", 2));
		Fingerprint oneBitsDiffHash = new CrawlhashFingerprint(Integer.parseInt("1000", 2));

		double distance = hash.getDistance(oneBitsDiffHash);
		assertThat(distance, is(1.0));

		assertTrue(hash.isNearDuplicate(oneBitsDiffHash, 2));
	}
	
	/**
	 * Tests isNearDuplicateHash/2 for a case that hashes are not near-duplicate
	 */
	@Test
	public void testIsNearDuplicateHashFingerprintZero() {
		Fingerprint fingerprint1 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		Fingerprint fingerprint2 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		
		boolean result = fingerprint1.isNearDuplicate(fingerprint2, 0);
		assertThat(result, is(true));
	}


	@Test
	public void testIsNearDuplicateHashFingerprintDoubleNot() {
		Fingerprint hash = new CrawlhashFingerprint(Integer.parseInt("1111", 2));
		Fingerprint threeBitsDiffHash = new CrawlhashFingerprint(Integer.parseInt("1000", 2));

		double distance = hash.getDistance(threeBitsDiffHash);
		assertThat(distance, is(3.0));

		assertFalse(hash.isNearDuplicate(threeBitsDiffHash, 2));
	}

	@Test
	public void testIsNearDuplicateHashFingerprintDoubleEqual() {
		Fingerprint hash1 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		Fingerprint hash2 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		assertTrue(hash1.isNearDuplicate(hash2, 0));
	}

	@Test
	public void testIsNearDuplicateHashFingerprintDoubleReverse() {
		Fingerprint hash1 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		Fingerprint hash2 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		assertEquals(hash1.isNearDuplicate(hash2, 0), hash2.isNearDuplicate(hash1, 0));
	}

	/**
	 * Test method for
	 * {@link com.crawljax.core.state.duplicatedetection.CrawlhashFingerprint#getDistance(com.crawljax.core.state.duplicatedetection.Fingerprint)}
	 * .
	 */
	@Test
	public void testGetDistanceDiff1() {
		Fingerprint hash1 = new CrawlhashFingerprint(Integer.parseInt("1001", 2));
		Fingerprint hash2 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		assertEquals(1, hash1.getDistance(hash2), 0.01);
	}

	@Test
	public void testGetDistanceSame() {
		Fingerprint hash1 = new CrawlhashFingerprint(Integer.parseInt("1111", 2));
		Fingerprint hash2 = new CrawlhashFingerprint(Integer.parseInt("1111", 2));
		assertEquals(0, hash1.getDistance(hash2), 0.01);
	}

	@Test
	public void testGetDistanceReverseEqual() {
		Fingerprint hash1 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		Fingerprint hash2 = new CrawlhashFingerprint(Integer.parseInt("1001", 2));
		assertEquals(hash1.getDistance(hash2), hash2.getDistance(hash1), 0.01);
	}

	@Test(expected = RuntimeException.class)
	public void testGetDistanceOtherObjectType() {
		Fingerprint hash1 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		hash1.getDistance(mock(Fingerprint.class));
	}

	@Test(expected = RuntimeException.class)
	public void testGetDistanceNull() {
		Fingerprint hash1 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		hash1.getDistance(null);
	}

	@Test
	public void testEqualsNull() {
		Fingerprint hash1 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		assertFalse(hash1.equals(null));
	}

	@Test
	public void testEqualsOtherObject() {
		Fingerprint hash1 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		assertFalse(hash1.equals(new Object()));
	}

	@Test
	public void testEqualsSame() {
		Fingerprint hash1 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		assertTrue(hash1.equals(hash1));
	}

	@Test
	public void testEqualsSameHash() {
		Fingerprint hash1 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		Fingerprint hash2 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		assertTrue(hash1.equals(hash2));
	}

	@Test
	public void testEqualsOtherHash() {
		Fingerprint hash1 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		Fingerprint hash2 = new CrawlhashFingerprint(Integer.parseInt("1111", 2));
		assertFalse(hash1.equals(hash2));
	}
	
	@Test(expected=DuplicateDetectionException.class)
	public void testInvalidThresholdException() {
		new CrawlhashFingerprint(Integer.parseInt("1000", 2), 42);
	}
	
	@Test(expected=DuplicateDetectionException.class)
	public void testInvalidThresholdException2() {
		new CrawlhashFingerprint(Integer.parseInt("1000", 2), -1);
	}
	
	@Test
	public void testCrawlhashFingerprint() {
		final double threshold = 2;
		Fingerprint fingerprint = new CrawlhashFingerprint(Integer.parseInt("1000", 2), threshold);
		assertNotNull(fingerprint.toString());
		assertEquals(fingerprint.getDefaultThreshold(),threshold,0.01);
		assertEquals(fingerprint.getThresholdLowerlimit(), 0, 0.01);
		assertEquals(fingerprint.getThresholdUpperlimit(), 32, 0.01);
	}
}
