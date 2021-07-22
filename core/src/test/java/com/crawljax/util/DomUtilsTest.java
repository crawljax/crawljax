package com.crawljax.util;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
	public void writeAndGetContents() throws IOException, TransformerException {
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
	
	@Test
	public void testLeafNodeSize() throws IOException {
		String html = "<html><body><p>bla</p></body></html>";
		Document doc = DomUtils.asDocument(html);
		try {
			assertThat(DomUtils.getAllLeafNodes(doc).getLength(), is(2));
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testLeafNodeSize2() throws IOException {
		String html = "<html><body><p>bla</p></body></html>";
		Document doc = DomUtils.asDocument(html);
		try {
			assertThat(DomUtils.getNumLeafNodes(doc.getElementsByTagName("body").item(0)), is(1));
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testGetText() throws IOException {
		//String html = "<html><body><p>bla</p> <p>bla</p></body></html>";
		//String html = "<HTML lang=\\\\\"en\\\\\"><!--PetClinic :: a Spring Framework demonstration--><HEAD><META http-equiv=\\\\\"Content-Type\\\\\" content=\\\\\"text/html; charset=UTF-8\\\\\"><META content=\\\\\"text/html; charset=UTF-8\\\\\" http-equiv=\\\\\"Content-Type\\\\\"><TITLE>PetClinic :: a Spring Framework demonstration</TITLE><LINK href=\\\\\"/petclinic/webjars/bootstrap/2.3.0/css/bootstrap.min.css;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\\\" rel=\\\\\"stylesheet\\\\\"><LINK href=\\\\\"/petclinic/resources/css/petclinic.css;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\\\" rel=\\\\\"stylesheet\\\\\"><!-- jquery-ui.js file is really big so we only load what we need instead of loading everything --><!-- jquery-ui.css file is not that big so we can afford to load it --><LINK href=\\\\\"/petclinic/webjars/jquery-ui/1.10.3/themes/base/jquery-ui.css;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\\\" rel=\\\\\"stylesheet\\\\\"></HEAD><BODY style=\\\\\"\\\\\"><DIV class=\\\\\"container\\\\\"><IMG src=\\\\\"/petclinic/resources/images/banner-graphic.png;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\\\"><DIV class=\\\\\"navbar\\\\\" style=\\\\\"width: 601px;\\\\\"><DIV class=\\\\\"navbar-inner\\\\\"><UL class=\\\\\"nav\\\\\"><LI style=\\\\\"width: 120px;\\\\\"><A href=\\\\\"/petclinic/;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\\\"><I class=\\\"icon-home\\\"></I>Home</A></LI><LI style=\\\"width: 150px;\\\"><A href=\\\"/petclinic/owners/find.html;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\"><I class=\\\"icon-search\\\"></I>Find owners</A></LI><LI style=\\\"width: 160px;\\\"><A href=\\\"/petclinic/vets.html;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\"><I class=\\\"icon-th-list\\\"></I>Veterinarians</A></LI><LI style=\\\"width: 110px;\\\"><A href=\\\"/petclinic/oups.html;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\" title=\\\"trigger a RuntimeException to see how it is handled\\\"><I class=\\\"icon-warning-sign\\\"></I>Error</A></LI></UL></DIV></DIV><H2>Welcome</H2><IMG src=\\\"/petclinic/resources/images/pets.png;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\"><TABLE class=\\\"footer\\\"><TBODY><TR><TD width=\\\"70%\\\"></TD><TD align=\\\"right\\\"><IMG alt=\\\"Sponsored by Pivotal\\\" src=\\\"/petclinic/resources/images/spring-pivotal-logo.png;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\"></TD></TR></TBODY></TABLE></DIV></BODY></HTML>";
		String html = "<HTML lang=\\\"en\\\"><!--\n" +
				"PetClinic :: a Spring Framework demonstration\n" +
				"--><HEAD><META http-equiv=\\\"Content-Type\\\" content=\\\"text/html; charset=UTF-8\\\">\n"
				+
				"    <META content=\\\"text/html; charset=UTF-8\\\" http-equiv=\\\"Content-Type\\\">\n"
				+
				"    <TITLE>PetClinic :: a Spring Framework demonstration</TITLE>\n" +
				"\n" +
				"\n" +
				"    \n" +
				"    <LINK href=\\\"/petclinic/webjars/bootstrap/2.3.0/css/bootstrap.min.css;jsessionid=278F291142D939304645517B5C814EC6\\\" rel=\\\"stylesheet\\\">\n"
				+
				"\n" +
				"    \n" +
				"    <LINK href=\\\"/petclinic/resources/css/petclinic.css;jsessionid=278F291142D939304645517B5C814EC6\\\" rel=\\\"stylesheet\\\">\n"
				+
				"\n" +
				"    \n" +
				"    \n" +
				"\n" +
				"	<!-- jquery-ui.js file is really big so we only load what we need instead of loading everything -->\n"
				+
				"    \n" +
				"    \n" +
				"\n" +
				"	\n" +
				"    \n" +
				"    \n" +
				"    <!-- jquery-ui.css file is not that big so we can afford to load it -->\n" +
				"    \n" +
				"    <LINK href=\\\"/petclinic/webjars/jquery-ui/1.10.3/themes/base/jquery-ui.css;jsessionid=278F291142D939304645517B5C814EC6\\\" rel=\\\"stylesheet\\\">\n"
				+
				"</HEAD><BODY style=\\\"\\\">\n" +
				"<DIV class=\\\"container\\\">\n" +
				"    \n" +
				"\n" +
				"\n" +
				"\n" +
				"<IMG src=\\\"/petclinic/resources/images/banner-graphic.png;jsessionid=278F291142D939304645517B5C814EC6\\\">\n"
				+
				"\n" +
				"<DIV class=\\\"navbar\\\" style=\\\"width: 601px;\\\">\n" +
				"    <DIV class=\\\"navbar-inner\\\">\n" +
				"        <UL class=\\\"nav\\\">\n" +
				"            <LI style=\\\"width: 120px;\\\"><A href=\\\"/petclinic/;jsessionid=278F291142D939304645517B5C814EC6\\\"><I class=\\\"icon-home\\\"></I>\n"
				+
				"                Home</A></LI>\n" +
				"            <LI style=\\\"width: 150px;\\\"><A href=\\\"/petclinic/owners/find.html;jsessionid=278F291142D939304645517B5C814EC6\\\"><I class=\\\"icon-search\\\"></I> Find owners</A></LI>\n"
				+
				"            <LI style=\\\"width: 160px;\\\"><A href=\\\"/petclinic/vets.html;jsessionid=278F291142D939304645517B5C814EC6\\\"><I class=\\\"icon-th-list\\\"></I> Veterinarians</A></LI>\n"
				+
				"            <LI style=\\\"width: 110px;\\\"><A href=\\\"/petclinic/oups.html;jsessionid=278F291142D939304645517B5C814EC6\\\" title=\\\"trigger a RuntimeException to see how it is handled\\\"><I class=\\\"icon-warning-sign\\\"></I> Error</A></LI>\n"
				+
				"        </UL>\n" +
				"    </DIV>\n" +
				"</DIV>\n" +
				"	\n" +
				"\n" +
				"    <H2>Welcome</H2>\n" +
				"    \n" +
				"    <IMG src=\\\"/petclinic/resources/images/pets.png;jsessionid=278F291142D939304645517B5C814EC6\\\">\n"
				+
				"\n" +
				"    \n" +
				"\n" +
				"<TABLE class=\\\"footer\\\">\n" +
				"    <TBODY><TR>\n" +
				"        <TD width=\\\"70%\\\"></TD>\n" +
				"        <TD align=\\\"right\\\"><IMG alt=\\\"Sponsored by Pivotal\\\" src=\\\"/petclinic/resources/images/spring-pivotal-logo.png;jsessionid=278F291142D939304645517B5C814EC6\\\"></TD>\n"
				+
				"    </TR>\n" +
				"</TBODY></TABLE>\n" +
				"\n" +
				"\n" +
				"\n" +
				"\n" +
				"</DIV>\n" +
				"\n" +
				"\n" +
				"\n" +
				"</BODY></HTML>";

		Document doc = DomUtils.asDocument(html);
		try {
			String content = DomUtils.getTextContent(doc, true);
			assertThat(content.contains("a Spring Framework"), is(true));
			content = DomUtils.getTextContent(doc, false);
			assertThat(content.contains("a Spring Framework"), is(false));
		} catch (Exception Ex) {

		}
	}

	@Test
	public void testGetDOMContent() throws IOException {
		//String html = "<html><body><p>bla</p> <p>bla</p></body></html>";
		//String html = "<HTML lang=\\\\\"en\\\\\"><!--PetClinic :: a Spring Framework demonstration--><HEAD><META http-equiv=\\\\\"Content-Type\\\\\" content=\\\\\"text/html; charset=UTF-8\\\\\"><META content=\\\\\"text/html; charset=UTF-8\\\\\" http-equiv=\\\\\"Content-Type\\\\\"><TITLE>PetClinic :: a Spring Framework demonstration</TITLE><LINK href=\\\\\"/petclinic/webjars/bootstrap/2.3.0/css/bootstrap.min.css;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\\\" rel=\\\\\"stylesheet\\\\\"><LINK href=\\\\\"/petclinic/resources/css/petclinic.css;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\\\" rel=\\\\\"stylesheet\\\\\"><!-- jquery-ui.js file is really big so we only load what we need instead of loading everything --><!-- jquery-ui.css file is not that big so we can afford to load it --><LINK href=\\\\\"/petclinic/webjars/jquery-ui/1.10.3/themes/base/jquery-ui.css;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\\\" rel=\\\\\"stylesheet\\\\\"></HEAD><BODY style=\\\\\"\\\\\"><DIV class=\\\\\"container\\\\\"><IMG src=\\\\\"/petclinic/resources/images/banner-graphic.png;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\\\"><DIV class=\\\\\"navbar\\\\\" style=\\\\\"width: 601px;\\\\\"><DIV class=\\\\\"navbar-inner\\\\\"><UL class=\\\\\"nav\\\\\"><LI style=\\\\\"width: 120px;\\\\\"><A href=\\\\\"/petclinic/;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\\\"><I class=\\\"icon-home\\\"></I>Home</A></LI><LI style=\\\"width: 150px;\\\"><A href=\\\"/petclinic/owners/find.html;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\"><I class=\\\"icon-search\\\"></I>Find owners</A></LI><LI style=\\\"width: 160px;\\\"><A href=\\\"/petclinic/vets.html;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\"><I class=\\\"icon-th-list\\\"></I>Veterinarians</A></LI><LI style=\\\"width: 110px;\\\"><A href=\\\"/petclinic/oups.html;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\" title=\\\"trigger a RuntimeException to see how it is handled\\\"><I class=\\\"icon-warning-sign\\\"></I>Error</A></LI></UL></DIV></DIV><H2>Welcome</H2><IMG src=\\\"/petclinic/resources/images/pets.png;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\"><TABLE class=\\\"footer\\\"><TBODY><TR><TD width=\\\"70%\\\"></TD><TD align=\\\"right\\\"><IMG alt=\\\"Sponsored by Pivotal\\\" src=\\\"/petclinic/resources/images/spring-pivotal-logo.png;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\"></TD></TR></TBODY></TABLE></DIV></BODY></HTML>";
		String html = "<HTML lang=\\\"en\\\"><!--\n" +
				"PetClinic :: a Spring Framework demonstration\n" +
				"--><HEAD><META http-equiv=\\\"Content-Type\\\" content=\\\"text/html; charset=UTF-8\\\">\n"
				+
				"    <META content=\\\"text/html; charset=UTF-8\\\" http-equiv=\\\"Content-Type\\\">\n"
				+
				"    <TITLE>PetClinic :: a Spring Framework demonstration</TITLE>\n" +
				"\n" +
				"\n" +
				"    \n" +
				"    <LINK href=\\\"/petclinic/webjars/bootstrap/2.3.0/css/bootstrap.min.css;jsessionid=278F291142D939304645517B5C814EC6\\\" rel=\\\"stylesheet\\\">\n"
				+
				"\n" +
				"    \n" +
				"    <LINK href=\\\"/petclinic/resources/css/petclinic.css;jsessionid=278F291142D939304645517B5C814EC6\\\" rel=\\\"stylesheet\\\">\n"
				+
				"\n" +
				"    \n" +
				"    \n" +
				"\n" +
				"	<!-- jquery-ui.js file is really big so we only load what we need instead of loading everything -->\n"
				+
				"    \n" +
				"    \n" +
				"\n" +
				"	\n" +
				"    \n" +
				"    \n" +
				"    <!-- jquery-ui.css file is not that big so we can afford to load it -->\n" +
				"    \n" +
				"    <LINK href=\\\"/petclinic/webjars/jquery-ui/1.10.3/themes/base/jquery-ui.css;jsessionid=278F291142D939304645517B5C814EC6\\\" rel=\\\"stylesheet\\\">\n"
				+
				"</HEAD><BODY style=\\\"\\\">\n" +
				"<DIV class=\\\"container\\\">\n" +
				"    \n" +
				"\n" +
				"\n" +
				"\n" +
				"<IMG src=\\\"/petclinic/resources/images/banner-graphic.png;jsessionid=278F291142D939304645517B5C814EC6\\\">\n"
				+
				"\n" +
				"<DIV class=\\\"navbar\\\" style=\\\"width: 601px;\\\">\n" +
				"    <DIV class=\\\"navbar-inner\\\">\n" +
				"        <UL class=\\\"nav\\\">\n" +
				"            <LI style=\\\"width: 120px;\\\"><A href=\\\"/petclinic/;jsessionid=278F291142D939304645517B5C814EC6\\\"><I class=\\\"icon-home\\\"></I>\n"
				+
				"                Home</A></LI>\n" +
				"            <LI style=\\\"width: 150px;\\\"><A href=\\\"/petclinic/owners/find.html;jsessionid=278F291142D939304645517B5C814EC6\\\"><I class=\\\"icon-search\\\"></I> Find owners</A></LI>\n"
				+
				"            <LI style=\\\"width: 160px;\\\"><A href=\\\"/petclinic/vets.html;jsessionid=278F291142D939304645517B5C814EC6\\\"><I class=\\\"icon-th-list\\\"></I> Veterinarians</A></LI>\n"
				+
				"            <LI style=\\\"width: 110px;\\\"><A href=\\\"/petclinic/oups.html;jsessionid=278F291142D939304645517B5C814EC6\\\" title=\\\"trigger a RuntimeException to see how it is handled\\\"><I class=\\\"icon-warning-sign\\\"></I> Error</A></LI>\n"
				+
				"        </UL>\n" +
				"    </DIV>\n" +
				"</DIV>\n" +
				"	\n" +
				"\n" +
				"    <H2>Welcome</H2>\n" +
				"    \n" +
				"    <IMG src=\\\"/petclinic/resources/images/pets.png;jsessionid=278F291142D939304645517B5C814EC6\\\">\n"
				+
				"\n" +
				"    \n" +
				"\n" +
				"<TABLE class=\\\"footer\\\">\n" +
				"    <TBODY><TR>\n" +
				"        <TD width=\\\"70%\\\"></TD>\n" +
				"        <TD align=\\\"right\\\"><IMG alt=\\\"Sponsored by Pivotal\\\" src=\\\"/petclinic/resources/images/spring-pivotal-logo.png;jsessionid=278F291142D939304645517B5C814EC6\\\"></TD>\n"
				+
				"    </TR>\n" +
				"</TBODY></TABLE>\n" +
				"\n" +
				"\n" +
				"\n" +
				"\n" +
				"</DIV>\n" +
				"\n" +
				"\n" +
				"\n" +
				"</BODY></HTML>";

		Document doc = DomUtils.asDocument(html);
		try {
			String content = DomUtils.getDOMContent(doc);
			assertThat(content.contains("Veterinarians"), is(true));
		} catch (Exception Ex) {

		}
	}

	@Test
	public void testGetDOMwithoutText() throws IOException {
		//String html = "<html><body><p>bla</p> <p>bla</p></body></html>";
		//String html = "<HTML lang=\\\\\"en\\\\\"><!--PetClinic :: a Spring Framework demonstration--><HEAD><META http-equiv=\\\\\"Content-Type\\\\\" content=\\\\\"text/html; charset=UTF-8\\\\\"><META content=\\\\\"text/html; charset=UTF-8\\\\\" http-equiv=\\\\\"Content-Type\\\\\"><TITLE>PetClinic :: a Spring Framework demonstration</TITLE><LINK href=\\\\\"/petclinic/webjars/bootstrap/2.3.0/css/bootstrap.min.css;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\\\" rel=\\\\\"stylesheet\\\\\"><LINK href=\\\\\"/petclinic/resources/css/petclinic.css;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\\\" rel=\\\\\"stylesheet\\\\\"><!-- jquery-ui.js file is really big so we only load what we need instead of loading everything --><!-- jquery-ui.css file is not that big so we can afford to load it --><LINK href=\\\\\"/petclinic/webjars/jquery-ui/1.10.3/themes/base/jquery-ui.css;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\\\" rel=\\\\\"stylesheet\\\\\"></HEAD><BODY style=\\\\\"\\\\\"><DIV class=\\\\\"container\\\\\"><IMG src=\\\\\"/petclinic/resources/images/banner-graphic.png;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\\\"><DIV class=\\\\\"navbar\\\\\" style=\\\\\"width: 601px;\\\\\"><DIV class=\\\\\"navbar-inner\\\\\"><UL class=\\\\\"nav\\\\\"><LI style=\\\\\"width: 120px;\\\\\"><A href=\\\\\"/petclinic/;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\\\"><I class=\\\"icon-home\\\"></I>Home</A></LI><LI style=\\\"width: 150px;\\\"><A href=\\\"/petclinic/owners/find.html;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\"><I class=\\\"icon-search\\\"></I>Find owners</A></LI><LI style=\\\"width: 160px;\\\"><A href=\\\"/petclinic/vets.html;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\"><I class=\\\"icon-th-list\\\"></I>Veterinarians</A></LI><LI style=\\\"width: 110px;\\\"><A href=\\\"/petclinic/oups.html;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\" title=\\\"trigger a RuntimeException to see how it is handled\\\"><I class=\\\"icon-warning-sign\\\"></I>Error</A></LI></UL></DIV></DIV><H2>Welcome</H2><IMG src=\\\"/petclinic/resources/images/pets.png;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\"><TABLE class=\\\"footer\\\"><TBODY><TR><TD width=\\\"70%\\\"></TD><TD align=\\\"right\\\"><IMG alt=\\\"Sponsored by Pivotal\\\" src=\\\"/petclinic/resources/images/spring-pivotal-logo.png;jsessionid=7ACB4DC9ADF0791BF12D9A28C8936DC3\\\"></TD></TR></TBODY></TABLE></DIV></BODY></HTML>";
		String html = "<HTML lang=\\\"en\\\"><!--\n" +
				"PetClinic :: a Spring Framework demonstration\n" +
				"--><HEAD><META http-equiv=\\\"Content-Type\\\" content=\\\"text/html; charset=UTF-8\\\">\n"
				+
				"    <META content=\\\"text/html; charset=UTF-8\\\" http-equiv=\\\"Content-Type\\\">\n"
				+
				"    <TITLE>PetClinic :: a Spring Framework demonstration</TITLE>\n" +
				"\n" +
				"\n" +
				"    \n" +
				"    <LINK href=\\\"/petclinic/webjars/bootstrap/2.3.0/css/bootstrap.min.css;jsessionid=278F291142D939304645517B5C814EC6\\\" rel=\\\"stylesheet\\\">\n"
				+
				"\n" +
				"    \n" +
				"    <LINK href=\\\"/petclinic/resources/css/petclinic.css;jsessionid=278F291142D939304645517B5C814EC6\\\" rel=\\\"stylesheet\\\">\n"
				+
				"\n" +
				"    \n" +
				"    \n" +
				"\n" +
				"	<!-- jquery-ui.js file is really big so we only load what we need instead of loading everything -->\n"
				+
				"    \n" +
				"    \n" +
				"\n" +
				"	\n" +
				"    \n" +
				"    \n" +
				"    <!-- jquery-ui.css file is not that big so we can afford to load it -->\n" +
				"    \n" +
				"    <LINK href=\\\"/petclinic/webjars/jquery-ui/1.10.3/themes/base/jquery-ui.css;jsessionid=278F291142D939304645517B5C814EC6\\\" rel=\\\"stylesheet\\\">\n"
				+
				"</HEAD><BODY style=\\\"\\\">\n" +
				"<DIV class=\\\"container\\\">\n" +
				"    \n" +
				"\n" +
				"\n" +
				"\n" +
				"<IMG src=\\\"/petclinic/resources/images/banner-graphic.png;jsessionid=278F291142D939304645517B5C814EC6\\\">\n"
				+
				"\n" +
				"<DIV class=\\\"navbar\\\" style=\\\"width: 601px;\\\">\n" +
				"    <DIV class=\\\"navbar-inner\\\">\n" +
				"        <UL class=\\\"nav\\\">\n" +
				"            <LI style=\\\"width: 120px;\\\"><A href=\\\"/petclinic/;jsessionid=278F291142D939304645517B5C814EC6\\\"><I class=\\\"icon-home\\\"></I>\n"
				+
				"                Home</A></LI>\n" +
				"            <LI style=\\\"width: 150px;\\\"><A href=\\\"/petclinic/owners/find.html;jsessionid=278F291142D939304645517B5C814EC6\\\"><I class=\\\"icon-search\\\"></I> Find owners</A></LI>\n"
				+
				"            <LI style=\\\"width: 160px;\\\"><A href=\\\"/petclinic/vets.html;jsessionid=278F291142D939304645517B5C814EC6\\\"><I class=\\\"icon-th-list\\\"></I> Veterinarians</A></LI>\n"
				+
				"            <LI style=\\\"width: 110px;\\\"><A href=\\\"/petclinic/oups.html;jsessionid=278F291142D939304645517B5C814EC6\\\" title=\\\"trigger a RuntimeException to see how it is handled\\\"><I class=\\\"icon-warning-sign\\\"></I> Error</A></LI>\n"
				+
				"        </UL>\n" +
				"    </DIV>\n" +
				"</DIV>\n" +
				"	\n" +
				"\n" +
				"    <H2>Welcome</H2>\n" +
				"    \n" +
				"    <IMG src=\\\"/petclinic/resources/images/pets.png;jsessionid=278F291142D939304645517B5C814EC6\\\">\n"
				+
				"\n" +
				"    \n" +
				"\n" +
				"<TABLE class=\\\"footer\\\">\n" +
				"    <TBODY><TR>\n" +
				"        <TD width=\\\"70%\\\"></TD>\n" +
				"        <TD align=\\\"right\\\"><IMG alt=\\\"Sponsored by Pivotal\\\" src=\\\"/petclinic/resources/images/spring-pivotal-logo.png;jsessionid=278F291142D939304645517B5C814EC6\\\"></TD>\n"
				+
				"    </TR>\n" +
				"</TBODY></TABLE>\n" +
				"\n" +
				"\n" +
				"\n" +
				"\n" +
				"</DIV>\n" +
				"\n" +
				"\n" +
				"\n" +
				"</BODY></HTML>";

		Document doc = DomUtils.asDocument(html);
		try {
			String content = DomUtils.getDOMWithoutContent(doc);
			assertThat(content.contains("Veterinarians"), is(false));
			doc = DomUtils.asDocument(content);
			String textContent = DomUtils.getTextContent(doc, false);
			assertThat(textContent.isEmpty(), is(true));
			textContent = DomUtils.getTextContent(doc, true);
			assertThat(textContent.isEmpty(), is(true));
		} catch (Exception Ex) {

		}
	}

	
	@Test
	public void getAllSubtreeNodesTest() throws IOException, XPathExpressionException {
		String html =
				"<body><div id='firstdiv'></div><div><span id='thespan'>"
						+ "<a id='thea'>test</a></span></div></body>";
		String xpath = "/HTML[1]/BODY[1]";
		Document dom = DomUtils.asDocument(html);
		Node parent = DomUtils.getElementByXpath(dom, xpath);
		NodeList childNodes = DomUtils.getAllSubtreeNodes(parent);
		System.out.println(childNodes.getLength());
		for(int i =0; i<childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			System.out.println(childNode.getNodeName());
		}
		
		assertTrue(childNodes.getLength() == 4);
		
	}
	
	@Test
	public void getAllAttributesTest() throws IOException, XPathExpressionException {
		String html =
				"<body><div id='firstdiv'></div><div><span id='thespan' xyz='xyz'>"
						+ "<a href='#' id='thea'>test</a></span></div></body>";
		Document dom = DomUtils.asDocument(html);
		
		Map<String, Set<String>> attributeMap = new HashMap<String, Set<String>>();
		DomUtils.getAllAttributes(dom, attributeMap, null);
		System.out.println(attributeMap);
		String[] array = {"firstdiv", "thespan", "thea"};
		List<String> expecteds = Arrays.asList(array);
		String[] actuals = attributeMap.get("id").toArray(new String[0]);
		assertTrue("Wrong return attributes", attributeMap.get("id").containsAll(expecteds));
//		assertArrayEquals("wrong return attribute values", expecteds, actuals);
	}
}