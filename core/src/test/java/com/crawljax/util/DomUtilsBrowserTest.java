package com.crawljax.util;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.browser.WebDriverBackedEmbeddedBrowser;
import com.crawljax.test.BrowserTest;
import com.google.common.collect.ImmutableSortedSet;

/**
 * Test for the Helper class.
 */
@Category(BrowserTest.class)
public class DomUtilsBrowserTest {

	private EmbeddedBrowser browser;

	@Before
	public void before() {
		browser = WebDriverBackedEmbeddedBrowser.withDriver(new FirefoxDriver(),
		        ImmutableSortedSet.<String> of(), 200, 300);
		URL url = DomUtilsBrowserTest.class.getResource("/site/index.html");
		browser.goToUrl(url);
	}

	/**
	 * Test get document from browser function.
	 */
	@Test
	public void testGetDocumentFromBrowser() throws SAXException, IOException {
		// TODO Stefan; Refactor out the direct use of FirefoxDriver

		String html = browser.getStrippedDom();
		assertNotNull(html);
		Document doc = DomUtils.asDocument(html);
		assertNotNull(doc);

		browser.close();
	}

}
