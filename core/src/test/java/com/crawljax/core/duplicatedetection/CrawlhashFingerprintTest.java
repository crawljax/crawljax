/**
 * 
 */
package com.crawljax.core.duplicatedetection;

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
	 * Test method for {@link com.crawljax.core.state.duplicatedetection.CrawlhashFingerprint#isNearDuplicateHash(com.crawljax.core.state.duplicatedetection.Fingerprint)}.
	 */
	@Test
	public void testIsNearDuplicateHashFingerprint() {
		Fingerprint hash = new CrawlhashFingerprint(Integer.parseInt("1001", 2),2);
		Fingerprint oneBitsDiffHash = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		
		double distance = hash.getDistance(oneBitsDiffHash);
		assertThat(distance, is(1.0));

		assertTrue(hash.isNearDuplicateHash(oneBitsDiffHash));
	}
	
	@Test
	public void testIsNearDuplicateHashFingerprintNot() {
		Fingerprint hash = new CrawlhashFingerprint(Integer.parseInt("1111", 2),2);
		Fingerprint threeBitsDiffHash = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		
		double distance = hash.getDistance(threeBitsDiffHash);
		assertThat(distance, is(3.0));
		
		assertFalse(hash.isNearDuplicateHash(threeBitsDiffHash));
	}
	
	@Test
	public void testIsNearDuplicateHashFingerprintEqual() {
		Fingerprint hash = new CrawlhashFingerprint(Integer.parseInt("1000", 2),0);
		Fingerprint hashSame = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		
		double distance = hash.getDistance(hashSame);
		assertThat(distance, is(0.0));
		
		assertTrue(hash.isNearDuplicateHash(hashSame));
	}

	@Test
	public void testIsNearDuplicateHashFingerprintReverse() {
		Fingerprint hash1 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		Fingerprint hash2 = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		assertEquals(hash1.isNearDuplicateHash(hash2, 0), hash2.isNearDuplicateHash(hash1, 0));
	}

	/**
	 * Test method for {@link com.crawljax.core.state.duplicatedetection.CrawlhashFingerprint#isNearDuplicateHash(com.crawljax.core.state.duplicatedetection.Fingerprint, double)}.
	 */
	@Test
	public void testIsNearDuplicateHashFingerprintDouble() {
		Fingerprint hash = new CrawlhashFingerprint(Integer.parseInt("1001", 2));
		Fingerprint oneBitsDiffHash = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		
		double distance = hash.getDistance(oneBitsDiffHash);
		assertThat(distance, is(1.0));

		assertTrue(hash.isNearDuplicateHash(oneBitsDiffHash, 2));
	}
	
	@Test
	public void testIsNearDuplicateHashFingerprintDoubleNot() {
		Fingerprint hash = new CrawlhashFingerprint(Integer.parseInt("1111", 2));
		Fingerprint threeBitsDiffHash = new CrawlhashFingerprint(Integer.parseInt("1000", 2));
		
		double distance = hash.getDistance(threeBitsDiffHash);
		assertThat(distance, is(3.0));
		
		assertFalse(hash.isNearDuplicateHash(threeBitsDiffHash, 2));
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
}
