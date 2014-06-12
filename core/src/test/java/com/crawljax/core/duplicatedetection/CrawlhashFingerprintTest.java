/**
 * 
 */
package com.crawljax.core.duplicatedetection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import com.crawljax.core.state.duplicatedetection.CrawlhashFingerprint;
import com.crawljax.core.state.duplicatedetection.Fingerprint;
/**
 * Tests for Crawlhash-fingerprint, 
 */
public class CrawlhashFingerprintTest {

	/**
	 * Test method for {@link com.crawljax.core.state.duplicatedetection.CrawlhashFingerprint#isNearDuplicateHash(com.crawljax.core.state.duplicatedetection.Fingerprint)}.
	 */
	@Test
	public void testIsNearDuplicateHashFingerprint() {
		Fingerprint hash1 = new CrawlhashFingerprint(Integer.parseInt("1001", 2),2);
		Fingerprint hash2 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		assertTrue(hash1.isNearDuplicateHash(hash2));
	}
	
	@Test
	public void testIsNearDuplicateHashFingerprintNot() {
		Fingerprint hash1 = new CrawlhashFingerprint(Integer.parseInt("1111", 2),2);
		Fingerprint hash2 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		assertFalse(hash1.isNearDuplicateHash(hash2));
	}
	
	@Test
	public void testIsNearDuplicateHashFingerprintEqual() {
		Fingerprint hash1 = new CrawlhashFingerprint(Integer.parseInt("1000", 2),0);
		Fingerprint hash2 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		assertTrue(hash1.isNearDuplicateHash(hash2));
	}

	@Test
	public void testIsNearDuplicateHashFingerprintReverse() {
		Fingerprint hash1 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		Fingerprint hash2 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		assertEquals(hash1.isNearDuplicateHash(hash2, 0),hash2.isNearDuplicateHash(hash1, 0));
	}

	/**
	 * Test method for {@link com.crawljax.core.state.duplicatedetection.CrawlhashFingerprint#isNearDuplicateHash(com.crawljax.core.state.duplicatedetection.Fingerprint, double)}.
	 */
	@Test
	public void testIsNearDuplicateHashFingerprintDouble() {
		Fingerprint hash1 = new CrawlhashFingerprint(Integer.parseInt("1001", 2));
		Fingerprint hash2 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		assertTrue(hash1.isNearDuplicateHash(hash2, 2));
	}
	
	@Test
	public void testIsNearDuplicateHashFingerprintDoubleNot() {
		Fingerprint hash1 = new CrawlhashFingerprint(Integer.parseInt("1111", 2));
		Fingerprint hash2 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		assertFalse(hash1.isNearDuplicateHash(hash2, 2));
	}
	
	@Test
	public void testIsNearDuplicateHashFingerprintDoubleEqual() {
		Fingerprint hash1 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		Fingerprint hash2 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		assertTrue(hash1.isNearDuplicateHash(hash2, 0));
	}
	
	@Test
	public void testIsNearDuplicateHashFingerprintDoubleReverse() {
		Fingerprint hash1 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		Fingerprint hash2 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		assertEquals(hash1.isNearDuplicateHash(hash2, 0),hash2.isNearDuplicateHash(hash1, 0));
	}


	/**
	 * Test method for {@link com.crawljax.core.state.duplicatedetection.CrawlhashFingerprint#getDistance(com.crawljax.core.state.duplicatedetection.Fingerprint)}.
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

	/**
	 * Test method for {@link com.crawljax.core.state.duplicatedetection.CrawlhashFingerprint#getHashesAsIntArray()}.
	 */
	@Test
	public void testGetHashesAsIntArray() {
		int hash = Integer.parseInt("1000", 2);
		Fingerprint fp = new CrawlhashFingerprint(hash);
		assertEquals(hash, fp.getHashesAsIntArray()[0]);
	}

	@Test(expected=RuntimeException.class)
	public void testGetDistanceOtherObjectType() {
		Fingerprint hash1 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		hash1.getDistance(mock(Fingerprint.class));
	}
	
	@Test(expected=RuntimeException.class)
	public void testGetDistanceNull() {
		Fingerprint hash1 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		hash1.getDistance(null);
	}
}
