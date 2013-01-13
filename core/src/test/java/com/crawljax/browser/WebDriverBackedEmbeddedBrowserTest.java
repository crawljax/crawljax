package com.crawljax.browser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.crawljax.core.CrawljaxException;
import com.crawljax.test.BrowserTest;
import com.crawljax.util.Helper;

@Category(BrowserTest.class)
public class WebDriverBackedEmbeddedBrowserTest {

	private final File index = new File("src/test/resources/site/iframe/index.html");

	@Test
	public void testGetDocument() throws Exception {
		// TODO Stefan; refactor out the direct use of the FirefoxDriver
		WebDriverBackedEmbeddedBrowser driver =
		        WebDriverBackedEmbeddedBrowser.withDriver(new FirefoxDriver(), null, 100, 100);

		Document doc;
		try {
			driver.goToUrl("file://" + index.getAbsolutePath());

			doc = Helper.getDocument(driver.getDom());
			NodeList frameNodes = doc.getElementsByTagName("IFRAME");
			assertEquals(2, frameNodes.getLength());

			doc = Helper.getDocument(driver.getDomWithoutIframeContent());
			frameNodes = doc.getElementsByTagName("IFRAME");
			assertEquals(2, frameNodes.getLength());
		} finally {
			driver.close();
		}

	}

	@Test
	public void saveScreenShot() throws CrawljaxException, IOException {
		// TODO Stefan; refactor out the direct use of the FirefoxDriver
		WebDriverBackedEmbeddedBrowser browser =
		        WebDriverBackedEmbeddedBrowser.withDriver(new FirefoxDriver(), null, 500, 500);

		File f = File.createTempFile("webdriverfirefox-test-screenshot", ".png");
		if (!f.exists()) {
			browser.goToUrl("file://" + index.getAbsolutePath());
			browser.saveScreenShot(f);
			assertTrue(f.exists());
			assertTrue(f.delete());
		}

		browser.close();
	}
}
