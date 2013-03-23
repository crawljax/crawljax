package com.crawljax.util;

import static org.junit.Assert.*;
import org.junit.Test;
import javax.xml.XMLConstants;
import com.crawljax.util.HtmlNamespace;

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
		
		testPrefix = "jiberish";
		assertEquals(XMLConstants.DEFAULT_NS_PREFIX, testNamespace.getNamespaceURI(testPrefix));
		
		testPrefix = "html";
		assertEquals("http://www.w3.org/1999/xhtml", testNamespace.getNamespaceURI(testPrefix));
		
		testPrefix = "xml";
		assertEquals(XMLConstants.XML_NS_URI, testNamespace.getNamespaceURI(testPrefix));
	}
	
}