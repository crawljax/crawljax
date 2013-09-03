package com.crawljax.util;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.TransformerException;
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
	 * @throws XPathExpressionException
	 * @throws IOException
	 */
	@Test
	public void testGetElementByXpath() throws XPathExpressionException, IOException {
		String html =
		        "<body><div id='firstdiv'></div><div><span id='thespan'>"
		                + "<a id='thea'>test</a></span></div></body>";
		String xpath = "/HTML[1]/BODY[1]/DIV[1]";
		Document dom = DomUtils.asDocument(html);
		assertNotNull(dom);

		Element elem = DomUtils.getElementByXpath(dom, xpath);
		assertNotNull(elem);
		assertEquals("ID: firstdiv id=firstdiv",
		        DomUtils.getElementString(elem).trim());

		xpath = "/HTML[1]/BODY[1]/DIV[2]/SPAN[1]";
		elem = DomUtils.getElementByXpath(dom, xpath);
		assertNotNull(elem);
		assertEquals("\"test\" ID: thespan id=thespan",
		        DomUtils.getElementString(elem).trim());

		xpath = "/HTML[1]/BODY[1]/DIV[2]/SPAN[1]/A[1]";
		elem = DomUtils.getElementByXpath(dom, xpath);
		assertNotNull(elem);
		assertEquals("\"test\" ID: thea id=thea",
		        DomUtils.getElementString(elem).trim());
	}

	/*
	 * Tests tag removal from a dom.
	 * @throws IOException
	 */
	@Test
	public void testRemoveTags() throws IOException {
		String html = "<body><div id='testdiv'</div><div style=\"colour:#FF0000\">"
		        + "<h>Header</h></div></body>";
		Document dom = DomUtils.asDocument(html);
		assertNotNull(dom);
		assertTrue(dom.getElementsByTagName("div").getLength() != 0);

		DomUtils.removeTags(dom, "div");
		assertTrue(dom.getElementsByTagName("div").getLength() == 0);
	}

	/*
	 * Tests the removal of <SCRIPT> tags.
	 * @throws IOException
	 */
	@Test
	public void testRemoveScriptTags() throws IOException {
		String html = "<body><script type=\"test/javascript\">" +
		        "document.write(\"Testing!\")</script></body>";

		Document dom = DomUtils.asDocument(html);
		assertNotNull(dom);
		assertTrue(dom.getElementsByTagName("script").getLength() != 0);

		DomUtils.removeScriptTags(dom);
		assertTrue(dom.getElementsByTagName("script").getLength() == 0);
	}

	/*
	 * Tests the string representation of a document.
	 * @throws IOException
	 */
	@Test
	public void testGetDocumentToString() throws IOException {
		String html = "<body><div id='testdiv'</div><div style=\"colour:#FF0000\">"
		        + "<h>Header</h></div></body>";

		String expectedDocString = "<HTML><HEAD><META http-equiv=\"Content-Type\"" +
		        " content=\"text/html; charset=UTF-8\"></HEAD><BODY><DIV id=\"testdiv\">" +
		        "</DIV><DIV style=\"colour:#FF0000\"><H>Header</H></DIV></BODY></HTML>";

		Document dom = DomUtils.asDocument(html);
		assertNotNull(dom);
		assertEquals(expectedDocString, DomUtils.getDocumentToString(dom).replace("\n", "")
		        .replace("\r", ""));
	}

	/*
	 * Tests getting the text value from an element.
	 * @throws IOException
	 */
	@Test
	public void testGetTextValue() throws IOException {
		String expectedText1 = "Testing title text";
		String expectedText2 = "Testing content test";
		String expectedText3 = "Testing alternative text";
		String html = "<body><br id='test1' title=\"" + expectedText1 + "\">"
		        + "<p id='test2'>" + expectedText2 + "</p>"
		        + "<br id='test3' alt=\"" + expectedText3 + "\"></body>";

		Document dom = DomUtils.asDocument(html);
		assertNotNull(dom);

		assertEquals(expectedText1,
		        DomUtils.getTextValue(dom.getElementById("test1")));

		assertEquals(expectedText2,
		        DomUtils.getTextValue(dom.getElementById("test2")));

		assertEquals(expectedText3,
		        DomUtils.getTextValue(dom.getElementById("test3")));
	}

	/*
	 * Tests removal of newlines from html strings.
	 */
	@Test
	public void testRemoveNewLines() {
		String html = "<HTML>\n<HEAD>\n<META http-equiv=\"Content-Type\"" +
		        " content=\"text/html; charset=UTF-8\"></HEAD>\n<BODY>\n<DIV id=\"testdiv\">" +
		        "</DIV><DIV style=\"colour:#FF0000\">\n<H>Header</H>\n</DIV>\n</BODY>\n</HTML>";

		String expectedString = "<HTML><HEAD><META http-equiv=\"Content-Type\"" +
		        " content=\"text/html; charset=UTF-8\"></HEAD><BODY><DIV id=\"testdiv\">" +
		        "</DIV><DIV style=\"colour:#FF0000\"><H>Header</H></DIV></BODY></HTML>";

		assertEquals(expectedString, DomUtils.removeNewLines(html));
	}

	/*
	 * Tests replacing strings using regular expressions.
	 */
	@Test
	public void testReplaceString() {
		String regex = "hello|world";
		String toReplace = "hello world helloworld worldhello";
		String expectedString = "testing testing testingtesting testingtesting";

		assertEquals(expectedString, DomUtils.replaceString(toReplace, regex, "testing"));
	}

	/*
	 * Test adding a / at the end of a folder path.
	 */
	@Test
	public void testAddFolderSlashIfNeeded() {
		String incompleteFolderName = "path/testpath";
		String expectedPath = incompleteFolderName + "/";

		assertEquals(expectedPath, DomUtils.addFolderSlashIfNeeded(incompleteFolderName));
	}

	@Test
	public void getElementAttributes() throws SAXException, IOException {
		Document dom;
		dom =
		        DomUtils.getDocumentNoBalance("<html><body><div class=\"bla\" "
		                + "id=\"test\">Bla</div></body></html>");
		assertEquals("class=bla id=test",
		        DomUtils.getAllElementAttributes(dom.getElementById("test")));
	}

	@Test
	public void writeAndGetContents() throws IOException, TransformerException, SAXException {
		File f = File.createTempFile("HelperTest.writeAndGetContents", ".tmp");
		DomUtils.writeDocumentToFile(
		        DomUtils.asDocument("<html><body><p>Test</p></body></html>"),
		        f.getAbsolutePath(), "html", 2);

		assertNotSame("", DomUtils.getTemplateAsString(f.getAbsolutePath()));

		assertTrue(f.exists());

	}

	/**
	 * Test get document function.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetDocument() throws IOException {
		String html = "<html><body><p/></body></html>";
		Document doc = DomUtils.asDocument(html);
		assertNotNull(doc);
	}

	@Test
	public void whenGetDocumentToStringNoCharEscape() throws IOException {
		String html = "<html><body><p>bla</p></body></html>";
		Document doc = DomUtils.asDocument(html);
		assertThat(DomUtils.getDocumentToString(doc).contains("<P>"), is(true));
	}
}