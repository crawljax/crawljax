/**
 * Created Dec 21, 2007
 */
package com.crawljax.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Test class for the XPathHelper class.
 * 
 * @author mesbah
 * @version $Id$
 */
public class XPathHelperTest {
	/**
	 * Check if XPath building works correctly.
	 * 
	 * @throws IOException
	 * @throws SAXException
	 */
	@Test
	public void testGetXpathExpression() throws SAXException, IOException {
		final String html =
		        "<body><div id='firstdiv'></div><div><span id='thespan'>"
		                + "<a id='thea'>test</a></span></div></body>";

		Document dom = Helper.getDocument(html);
		assertNotNull(dom);

		// first div
		String expectedXpath = "/HTML[1]/BODY[1]/DIV[1]";
		String xpathExpr = XPathHelper.getXPathExpression(dom.getElementById("firstdiv"));
		assertEquals(expectedXpath, xpathExpr);

		// span
		expectedXpath = "/HTML[1]/BODY[1]/DIV[2]/SPAN[1]";
		xpathExpr = XPathHelper.getXPathExpression(dom.getElementById("thespan"));
		assertEquals(expectedXpath, xpathExpr);

		// a
		expectedXpath = "/HTML[1]/BODY[1]/DIV[2]/SPAN[1]/A[1]";
		xpathExpr = XPathHelper.getXPathExpression(dom.getElementById("thea"));
		assertEquals(expectedXpath, xpathExpr);
	}

	@Test
	public void testXPathLocation() {
		String html = "<HTML><LINK foo=\"bar\">woei</HTML>";
		String xpath = "/HTML[1]/LINK[1]";
		int start = XPathHelper.getXPathLocation(html, xpath);
		int end = XPathHelper.getCloseElementLocation(html, xpath);

		assertEquals(6, start);
		assertEquals(22, end);
	}

	@Test
	public void formatXPath() {
		String xPath = "//ul[@CLASS=\"Test\"]";
		assertEquals("//UL[@class=\"Test\"]", XPathHelper.formatXPath(xPath));
	}

	@Test
	public void formatXPathAxes() {
		String xPath = "//ancestor-or-self::div[@CLASS,'foo']";
		assertEquals("//ancestor-or-self::DIV[@class,'foo']", XPathHelper.formatXPath(xPath));
	}

	@Test
	public void getLastElementOfXPath() {
		String xPath = "/HTML/BODY/DIV/UL/LI[@class=\"Test\"]";
		assertEquals("LI", XPathHelper.getLastElementXPath(xPath));
	}

	@Test
	public void stripXPathToElement() {
		String xPath = "/HTML/BODY/DIV/UL/LI[@class=\"Test\"]";
		assertEquals("/HTML/BODY/DIV/UL/LI", XPathHelper.stripXPathToElement(xPath));
	}
}
