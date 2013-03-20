package com.crawljax.core.largetests;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assume.assumeThat;

import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.test.BrowserTest;

@Category(BrowserTest.class)
public class LargeChromeTest extends LargeTestBase {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		assumeThat(System.getProperty("webdriver.chrome.driver"), notNullValue());
	}

	@Override
	BrowserConfiguration getBrowserConfiguration() {
		return new BrowserConfiguration(BrowserType.chrome);
	}

	@Override
	long getTimeOutAfterReloadUrl() {
		return 100;
	}

	@Override
	long getTimeOutAfterEvent() {
		return 100;
	}
}
