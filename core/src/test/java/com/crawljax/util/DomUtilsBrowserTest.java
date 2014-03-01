package com.crawljax.util;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import com.crawljax.browser.BrowserProvider;
import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.test.BrowserTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Test for the Helper class.
 */
@Category(BrowserTest.class)
public class DomUtilsBrowserTest {


	@Rule
	public BrowserProvider provider = new BrowserProvider();

	private EmbeddedBrowser browser;

	@Before
	public void before() throws URISyntaxException {
		browser = provider.newEmbeddedBrowser();
		URL url = DomUtilsBrowserTest.class.getResource("/site/index.html");
		browser.goToUrl(url.toURI());
	}

	/**
	 * Test get document from browser function.
	 */
	@Test
	public void testGetDocumentFromBrowser() throws SAXException, IOException {

		String html = browser.getStrippedDom();
		assertNotNull(html);
		Document doc = DomUtils.asDocument(html);
		assertNotNull(doc);

		browser.close();
	}

}
