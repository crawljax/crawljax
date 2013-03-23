package com.crawljax.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


public class DomUtilsTest {

	/**
	 * Test get string representation of an element.
	 * 
	 * @throws IOException
	 * @throws SAXException
	 */
	@Test
	public void testGetElementString() throws IOException, SAXException {
		Document dom;
		dom = DomUtils.getDocumentNoBalance("<html><body><div class=\"bla\" "
		        + "id=\"test\">Test Str</div></body></html>");
		
		assertEquals("\"Test Str\" ID: test class=bla id=test",
				DomUtils.getElementString(dom.getElementById("test")).trim()); 
	}
	
	/*
	 * Tests getting an element from an xpath. 
	 * 
	 * @throws XPathExpressionException
	 * @throws IOException
	 */
	@Test
	public void testGetElementByXpath() throws XPathExpressionException, IOException {
		final String html =
		        "<body><div id='firstdiv'></div><div><span id='thespan'>"
		                + "<a id='thea'>test</a></span></div></body>";
		String xpath = "/HTML[1]/BODY[1]/DIV[1]";
		Document dom = DomUtils.asDocument(html);
		assertNotNull(dom);
		
		Element elem = DomUtils.getElementByXpath(dom, xpath);
		assertNotNull(elem); 
		assertEquals("ID: firstdiv id=firstdiv", DomUtils.getElementString(elem).trim()); 
		
		xpath = "/HTML[1]/BODY[1]/DIV[2]/SPAN[1]";
		elem = DomUtils.getElementByXpath(dom, xpath);
		assertNotNull(elem); 
		assertEquals("\"test\" ID: thespan id=thespan", DomUtils.getElementString(elem).trim());
		
		xpath = "/HTML[1]/BODY[1]/DIV[2]/SPAN[1]/A[1]";
		elem = DomUtils.getElementByXpath(dom, xpath);
		assertNotNull(elem);
		assertEquals("\"test\" ID: thea id=thea", DomUtils.getElementString(elem).trim()); 
	}
	
	/*
	 * Tests the removal of <SCRIPT> tags.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testRemoveScriptTags() throws IOException {
		final String html = "<body><script type=\"test/javascript\">" + 
				"document.write(\"Testing!\")</script></body>";
		
		Document dom = DomUtils.asDocument(html); 
		assertNotNull(dom); 
		assertTrue(dom.getElementsByTagName("script").getLength() != 0);
		
		DomUtils.removeScriptTags(dom); 
		assertTrue(dom.getElementsByTagName("script").getLength() == 0); 
	}
}