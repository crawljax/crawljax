package com.crawljax.util;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Test class for the XPathHelper class.
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

		Document dom = DomUtils.asDocument(html);
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
	public void whenWildcardsUsedXpathShouldFindTheElements() throws Exception {
		String html =
		        "<body>" + "<DIV><P>Bla</P><P>Bla2</P></DIV>"
		                + "<DIV id='exclude'><P>Ex</P><P>Ex2</P></DIV>" + "</body>";
		String xpathAllp = "//DIV//P";
		String xpathOnlyExcludedP = "//DIV[@id='exclude']//P";
		NodeList nodes = XPathHelper.evaluateXpathExpression(html, xpathAllp);
		assertThat(nodes.getLength(), is(4));

		nodes = XPathHelper.evaluateXpathExpression(html, xpathOnlyExcludedP);
		assertThat(nodes.getLength(), is(2));
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
		assertThat(XPathHelper.formatXPath("//ul/a"), is("//UL/A"));
		assertThat(XPathHelper.formatXPath("/div//span"), is("/DIV//SPAN"));
		assertThat(XPathHelper.formatXPath("//ul[@CLASS=\"Test\"]"), is("//UL[@class=\"Test\"]"));
		assertThat(XPathHelper.formatXPath("//ul[@CLASS=\"Test\"]/a"),
		        is("//UL[@class=\"Test\"]/A"));

	}

	@Test
	public void formatXpathWithDoubleSlashes() {
		String xpath = "//div[@id='dontClick']//a";
		assertThat(XPathHelper.formatXPath(xpath), is("//DIV[@id='dontClick']//A"));
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