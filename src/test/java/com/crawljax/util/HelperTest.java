package com.crawljax.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.browser.WebDriverBackedEmbeddedBrowser;

/**
 * Test for the Helper class.
 * 
 * @author Ali Mesbah
 */
public class HelperTest {

	private static final String INDEX = "src/test/site/index.html";

	/**
	 * Test get document function.
	 */
	@Test
	public void testGetDocument() {
		String html = "<html><body><p/></body></html>";

		try {
			Document doc = Helper.getDocument(html);
			assertNotNull(doc);
		} catch (SAXException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}

	}

	/**
	 * Test get document from browser function.
	 */
	@Test
	public void testGetDocumentFromBrowser() {
		// TODO Stefan; Refactor out the direct use of FirefoxDriver
		EmbeddedBrowser browser =
		        WebDriverBackedEmbeddedBrowser.withDriver(new FirefoxDriver(), null, 200, 300);
		File index = new File(INDEX);
		String html = "";
		try {
			browser.goToUrl("file://" + index.getAbsolutePath());
		} catch (Exception e1) {
			fail(e1.getMessage());
		}

		try {
			html = browser.getDom();
			assertNotNull(html);
		} catch (Exception e1) {
			e1.printStackTrace();
			fail(e1.getMessage());

		}

		try {
			Document doc = Helper.getDocument(html);
			assertNotNull(doc);

		} catch (Exception e1) {
			fail(e1.getMessage());
		}

		try {
			browser.close();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		browser = null;

	}

	@Test
	public void isLinkExternal() {
		assertTrue(Helper.isLinkExternal("http://crawljax.com", "http://google.com"));
		assertTrue(Helper.isLinkExternal("http://crawljax.com", "file:///test/"));
		assertFalse(Helper.isLinkExternal("http://crawljax.com/download",
		        "http://crawljax.com/about"));
		// This is done intentional to capture miss formed urls as local so crawljax will process
		// them
		assertFalse("Missformed link is not external", Helper.isLinkExternal(
		        "http://crawljax.com", "http"));

		assertFalse("link and base are the same (http)", Helper.isLinkExternal(
		        "http://crawljax.com", "http://crawljax.com"));

		assertFalse("link and base are the same (https)", Helper.isLinkExternal(
		        "https://crawljax.com", "https://crawljax.com"));

		assertFalse("link and base are the same (file)", Helper.isLinkExternal(
		        "file:///tmp/index.html", "file:///tmp/index.html"));

		assertFalse("Sub dir is not external for file", Helper.isLinkExternal(
		        "file:///tmp/index.html", "file:///tmp/subdir/index.html"));

		assertFalse("Sub dirs is not external for http", Helper.isLinkExternal(
		        "http://crawljax.com", "http://crawljax.com/sub/dir/about.html"));

		assertFalse("Https link from http base is not external", Helper.isLinkExternal(
		        "http://crawljax.com", "https://crawljax.com/about.html"));
		assertFalse("Https link from https base is not external", Helper.isLinkExternal(
		        "https://crawljax.com", "https://crawljax.com/about.html"));
		assertFalse("Http link from https base is not external", Helper.isLinkExternal(
		        "https://crawljax.com", "http://crawljax.com/about.html"));

		assertFalse("relative link from https base is not external", Helper.isLinkExternal(
		        "https://crawljax.com", "about.html"));
		assertFalse("relative link from http base is not external", Helper.isLinkExternal(
		        "http://crawljax.com", "about.html"));

		assertFalse("root link from http base is not external", Helper.isLinkExternal(
		        "http://crawljax.com", "/about.html"));
		assertFalse("root link from https base is not external", Helper.isLinkExternal(
		        "https://crawljax.com", "/about.html"));

		assertFalse("relative link from file base is not external", Helper.isLinkExternal(
		        "file:///tmp/index.html", "about.html"));

		assertTrue("root link from file base is external", Helper.isLinkExternal(
		        "file://tmp/index.html", "/about.html"));
	}

	@Test
	public void getBaseUrl() {
		assertEquals("http://crawljax.com", Helper.getBaseUrl("http://crawljax.com/about/"));

	}

	@Test
	public void getElementAttributes() {
		Document dom;
		try {
			dom =
			        Helper.getDocumentNoBalance("<html><body><div class=\"bla\" "
			                + "id=\"test\">Bla</div></body></html>");
			assertEquals("class=bla id=test", Helper.getAllElementAttributes(dom
			        .getElementById("test")));
		} catch (Exception e) {
			fail("Exception caught");
		}
	}

	@Test
	public void directoryCheck() {
		String directory = "test-123-123";
		File dir = new File(directory);
		if (!dir.exists()) {
			try {
				Helper.directoryCheck(directory);
			} catch (IOException e) {
				fail("Error creating directory");
			}
			if (!dir.exists()) {
				fail("Directory not created");
			} else {
				assertTrue(dir.delete());
			}
		}
	}

	@Test
	public void getVarFromQueryString() {
		assertEquals("home", Helper.getVarFromQueryString("page",
		        "?sub=1&userid=123&page=home&goto=0"));
	}

	@Test
	public void writeAndGetContents() {
		File f = new File("helper-write-and-get-contents-test.txt");

		if (f.exists()) {
			assertTrue(f.delete());
		}

		try {
			Helper
			        .writeDocumentToFile(Helper
			                .getDocument("<html><body><p>Test</p></body></html>"), f.getName(),
			                "html", 2);

			assertNotSame("", Helper.getContent(f));

			assertNotSame("", Helper.getTemplateAsString(f.getName()));

		} catch (Exception e) {
			e.printStackTrace();
		}

		assertTrue(f.exists());

	}
}
