package com.crawljax.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.xml.XMLConstants;

import org.junit.Test;

public class HtmlNamespaceTest {

	@Test
	public void testgetNamespaceURI() {
		HtmlNamespace testNamespace = new HtmlNamespace();

		String testPrefix = null;
		boolean testPass = false;

		try {
			testNamespace.getNamespaceURI(testPrefix);
		} catch (NullPointerException e) {
			testPass = true;
		}
		assertTrue(testPass);

		assertEquals(XMLConstants.DEFAULT_NS_PREFIX, testNamespace.getNamespaceURI("gibberish"));

		assertEquals("http://www.w3.org/1999/xhtml", testNamespace.getNamespaceURI("html"));

		assertEquals(XMLConstants.XML_NS_URI, testNamespace.getNamespaceURI("xml"));
	}

}