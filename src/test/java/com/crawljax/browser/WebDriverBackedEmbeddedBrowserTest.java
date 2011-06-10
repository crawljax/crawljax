package com.crawljax.browser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.crawljax.core.CrawljaxException;
import com.crawljax.util.Helper;

public class WebDriverBackedEmbeddedBrowserTest {

	@Test
	public void testGetDocument() {
		File index = new File("src/test/site/iframe/index.html");
		// TODO Stefan; refactor out the direct use of the FirefoxDriver
		WebDriverBackedEmbeddedBrowser driver =
		        WebDriverBackedEmbeddedBrowser.withDriver(new FirefoxDriver(), null, 100, 100);

		Document doc;
		try {
			driver.goToUrl("file://" + index.getAbsolutePath());

			doc = Helper.getDocument(driver.getDom());
			NodeList frameNodes = doc.getElementsByTagName("IFRAME");
			assertEquals(4, frameNodes.getLength());

			doc = Helper.getDocument(driver.getDomWithoutIframeContent());
			frameNodes = doc.getElementsByTagName("IFRAME");
			assertEquals(2, frameNodes.getLength());

		} catch (SAXException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		finally {
			driver.close();
		}

	}

	@Test
	public void saveScreenShot() {
		// TODO Stefan; refactor out the direct use of the FirefoxDriver
		WebDriverBackedEmbeddedBrowser browser =
		        WebDriverBackedEmbeddedBrowser.withDriver(new FirefoxDriver(), null, 500, 500);

		File f = new File("webdriverfirefox-test-screenshot.png");
		if (!f.exists()) {
			browser.goToUrl("http://google.com");
			try {
				browser.saveScreenShot(f);
			} catch (CrawljaxException e) {
				fail(e.getMessage());
			}
			assertTrue(f.exists());
			assertTrue(f.delete());
		}

		browser.close();
	}
}
