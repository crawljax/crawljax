package com.crawljax.browser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import com.crawljax.core.CrawljaxException;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.RunWithWebServer;
import com.crawljax.util.DomUtils;
import com.google.common.collect.ImmutableSortedSet;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

@Category(BrowserTest.class)
public class WebDriverBackedEmbeddedBrowserTest {

	@ClassRule
	public static final RunWithWebServer SERVER = new RunWithWebServer("/site/iframe");

	@Rule
	public final BrowserProvider provider = new BrowserProvider();

	@Test
	public void testGetDocument() throws Exception {
		// TODO Stefan; refactor out the direct use of the FirefoxDriver
		WebDriverBackedEmbeddedBrowser driver =
		  WebDriverBackedEmbeddedBrowser.withDriver(provider.newBrowser(),
			ImmutableSortedSet.<String>of(), 100, 100);

		Document doc;
		driver.goToUrl(SERVER.getSiteUrl());

		doc = DomUtils.asDocument(driver.getStrippedDom());
		NodeList frameNodes = doc.getElementsByTagName("IFRAME");
		assertEquals(2, frameNodes.getLength());

		doc = DomUtils.asDocument(driver.getStrippedDomWithoutIframeContent());
		frameNodes = doc.getElementsByTagName("IFRAME");
		assertEquals(2, frameNodes.getLength());


	}

	@Test
	public void saveScreenShot() throws CrawljaxException, IOException {
		// TODO Stefan; refactor out the direct use of the FirefoxDriver
		WebDriverBackedEmbeddedBrowser browser =
		  WebDriverBackedEmbeddedBrowser.withDriver(provider.newBrowser(),
			ImmutableSortedSet.<String>of(), 500, 500);

		File f = File.createTempFile("webdriverfirefox-test-screenshot", ".png");
		if (!f.exists()) {
			browser.goToUrl(SERVER.getSiteUrl());
			browser.saveScreenShot(f);
			assertTrue(f.exists());
			assertTrue(f.delete());
		}

		browser.close();
	}
}
