package com.crawljax.browser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import javax.transaction.NotSupportedException;

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
		WebDriverBackedEmbeddedBrowser driver =
		        WebDriverBackedEmbeddedBrowser.withDriver(new FirefoxDriver(), null, 100, 100);

		Document doc;
		try {
			driver.goToUrl("file://" + index.getAbsolutePath());
			System.out.println("DOM with frames: " + driver.getDom());
			doc = Helper.getDocument(driver.getDom());
			NodeList frameNodes = doc.getElementsByTagName("IFRAME");
			assertEquals(5, frameNodes.getLength());

			doc = Helper.getDocument(driver.getDomWithoutIframeContent());
			frameNodes = doc.getElementsByTagName("IFRAME");
			assertEquals(3, frameNodes.getLength());

		} catch (SAXException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (CrawljaxException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		driver.close();

	}

	@Test
	public void saveScreenShot() {
		WebDriverBackedEmbeddedBrowser browser =
		        WebDriverBackedEmbeddedBrowser.withDriver(new FirefoxDriver(), null, 500, 500);

		File f = new File("webdriverfirefox-test-screenshot.png");
		if (!f.exists()) {
			try {
				browser.goToUrl("http://google.com");
			} catch (CrawljaxException e) {
				fail("Could not go to url.");
			}
			try {
				browser.saveScreenShot(f);
			} catch (NotSupportedException e) {
				fail(e.getMessage());
			}
			assertTrue(f.exists());
			f.delete();
		}

		browser.close();
	}
}
