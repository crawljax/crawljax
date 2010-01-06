/**
 * Created Dec 21, 2007
 */
package com.crawljax.util;

import junit.framework.TestCase;

import org.w3c.dom.Document;

/**
 * TODO: DOCUMENT ME!
 * 
 * @author mesbah
 * @version $Id: XPathHelperTest.java 6288 2009-12-23 16:31:08Z frank $
 */
public class XPathHelperTest extends TestCase {
	/**
	 * TODO: DOCUMENT ME!
	 */
	public void testGetXpathExpression() {
		final String html =
		        "<body><div id='firstdiv'></div><div><span id='thespan'>"
		                + "<a id='thea'>test</a></span></div></body>";

		try {
			Document dom = Helper.getDocument(html);
			assertNotNull(dom);

			// first div
			String expectedXpath = "/HTML[1]/BODY[1]/DIV[1]";
			String xpathExpr = XPathHelper.getXpathExpression(dom.getElementById("firstdiv"));
			assertEquals(expectedXpath, xpathExpr);

			// span
			expectedXpath = "/HTML[1]/BODY[1]/DIV[2]/SPAN[1]";
			xpathExpr = XPathHelper.getXpathExpression(dom.getElementById("thespan"));
			assertEquals(expectedXpath, xpathExpr);

			// a
			expectedXpath = "/HTML[1]/BODY[1]/DIV[2]/SPAN[1]/A[1]";
			xpathExpr = XPathHelper.getXpathExpression(dom.getElementById("thea"));
			assertEquals(expectedXpath, xpathExpr);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public void testXPathLocation() {
		String html = "<HTML><LINK foo=\"bar\">woei</HTML>";
		String xpath = "/HTML[1]/LINK[1]";
		int start = XPathHelper.getXPathLocation(html, xpath);
		int end = XPathHelper.getCloseElementLocation(html, xpath);

		System.out.println(html.substring(start, end));
	}

}
