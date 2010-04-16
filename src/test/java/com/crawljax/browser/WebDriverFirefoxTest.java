package com.crawljax.browser;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.crawljax.core.CrawljaxException;

public class WebDriverFirefoxTest {
	private static WebDriverFirefox browser;

	@BeforeClass
	public static void setup() {
		browser = new WebDriverFirefox(new ArrayList<String>(), 500, 500);
	}

	@Test
	public void saveScreenShot() {
		File f = new File("webdriverfirefox-test-screenshot.png");
		if (!f.exists()) {
			try {
				browser.goToUrl("http://google.com");
			} catch (CrawljaxException e) {
				fail("Could not go to url.");
			}
			browser.saveScreenShot(f);
			assertTrue(f.exists());
			f.delete();
		}
	}

	@Test
	public void cloneTest() {
		EmbeddedBrowser newBrowser = browser.clone();
		try {
			browser.goToUrl("http://crawljax.com");

			newBrowser.goToUrl("http://google.com");
			newBrowser.close();

			browser.goToUrl("http://google.com");
		} catch (CrawljaxException e) {
			fail("Could not browse to url");
		}
	}

	@AfterClass
	public static void finish() {
		browser.close();
	}
}
