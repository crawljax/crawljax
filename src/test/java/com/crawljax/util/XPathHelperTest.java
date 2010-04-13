/**
 * Created Dec 21, 2007
 */
package com.crawljax.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Test class for the XPathHelper class.
 * 
 * @author mesbah
 * @version $Id$
 */
public class XPathHelperTest {
	/**
	 * Check if XPath building works correctly.
	 */
	@Test
	public void testGetXpathExpression() {
		final String html =
		        "<body><div id='firstdiv'></div><div><span id='thespan'>"
		                + "<a id='thea'>test</a></span></div></body>";

		try {
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
		} catch (Exception e) {
			fail(e.getMessage());
		}
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
