package com.crawljax.util;

import static org.junit.Assert.assertNotNull;
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
		EmbeddedBrowser browser = new WebDriverFirefox();
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
}
