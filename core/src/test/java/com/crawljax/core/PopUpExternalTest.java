package com.crawljax.core;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.crawljax.browser.WebDriverBackedEmbeddedBrowser;

public class PopUpExternalTest {

	private WebDriverBackedEmbeddedBrowser browser;
	private WebDriver driver;

	@Before
	public void setUp() {
		driver = new FirefoxDriver();
		browser = WebDriverBackedEmbeddedBrowser.withDriver(driver);
	}

	@Test
	public void testExternalPopUpHandling() throws MalformedURLException {
		browser.goToUrl(new URL("http://lateensoft.host56.com/test/EECE-310/"));
		long numberOfInternalPopUps =
		        (long) browser.executeJavaScript("return window.numberOfInternalPopUps;");
		System.out.println(numberOfInternalPopUps);
		assertEquals( numberOfInternalPopUps, 2 );
	}
}
