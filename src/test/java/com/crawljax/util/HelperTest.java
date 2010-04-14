package com.crawljax.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.browser.WebDriverFirefox;

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
		EmbeddedBrowser browser = new WebDriverFirefox(null, 200, 300);
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
				dir.delete();
			}
		}
	}

	@Test
	public void getVarFromQueryString() {
		assertEquals("home", Helper.getVarFromQueryString("page",
		        "?sub=1&userid=123&page=home&goto=0"));
	}
}
