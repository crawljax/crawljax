package com.crawljax.core.duplicatedetection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.crawljax.core.state.StateVertex;
import com.crawljax.core.state.StateVertexImpl;
import com.crawljax.core.state.duplicatedetection.NearDuplicateDetection;
import com.crawljax.core.state.duplicatedetection.NearDuplicateDetectionFactory;

public class NearDuplicateDetectionTest {
	
	NearDuplicateDetection ndd = NearDuplicateDetectionFactory.getInstance();
	StateVertex v = new StateVertexImpl(1, "http://demo.crawljax.com", "State1", "<html><body><h1>Test</h1></body></html>", "<html><body><h1></h1></body></html>");
	
	@Test
	public void testDuplicateOnSameState() {
		boolean duplicate = ndd.isNearDuplicateHash(ndd.generateHash(v.getDom()),ndd.generateHash(v.getDom()));
		assertEquals(true, duplicate);
	}
	
	@Test
	public void testDuplicateOnNewState() {
		StateVertex w = new StateVertexImpl(1, "http://demo.crawljax.com", "State1", "<html><body><h1>Test</h1></body></html>", "<html><body><h1></h1></body></html>");
		boolean duplicate = ndd.isNearDuplicateHash(ndd.generateHash(v.getDom()),ndd.generateHash(w.getDom()));
		assertEquals(true, duplicate);
	}
	
	@Test
	public void testNotDuplicate() {
		StateVertex w = new StateVertexImpl(1, "http://test.com", "State5", "<p>hello world</p>", "<p></p>");
		boolean duplicate = ndd.isNearDuplicateHash(ndd.generateHash(v.getDom()),ndd.generateHash(w.getDom()));
		assertEquals(false, duplicate);
	}
	
	@Test
	public void testSameContentDifferentSite() {
		StateVertex w = new StateVertexImpl(1, "http://test.com", "Index", "<html><body><h1>Test</h1></body></html>", "<html><body><h1></h1></body></html>");
		boolean duplicate = ndd.isNearDuplicateHash(ndd.generateHash(v.getDom()),ndd.generateHash(w.getDom()));
		assertEquals(true, duplicate);
	}
	
	@Test
	public void testSameDomToSameHash() {
		StateVertex w = new StateVertexImpl(1, "http://test.com", "Index", "<html><body><h1>Test</h1></body></html>", "<html><body><h1></h1></body></html>");
		long hashOfvFromDom = ndd.generateHash(v.getDom());
		long hashOfwFromDom = ndd.generateHash(w.getDom());
		assertEquals(hashOfvFromDom, hashOfwFromDom);
	}
	
	@Test
	public void testDifferendDomToDifferendHash() {
		StateVertex w = new StateVertexImpl(1, "http://test.com", "State5", "<p>hello world</p>", "<p></p>");
		long hashOfvFromDom = ndd.generateHash(v.getDom());
		long hashOfwFromDom = ndd.generateHash(w.getDom());
		assertTrue(hashOfvFromDom != hashOfwFromDom);
	}
}
