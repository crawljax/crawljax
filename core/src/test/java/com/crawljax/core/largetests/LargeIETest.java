package com.crawljax.core.largetests;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assume.assumeThat;

import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.test.BrowserTest;

@Category(BrowserTest.class)
public class LargeIETest extends LargeTestBase {

	@BeforeClass
	public static void setUpBeforeClass() throws CrawljaxException {
		assumeThat(System.getProperty("os.name").toLowerCase(), containsString("windows"));
	}

	@Override
	BrowserConfiguration getBrowserConfiguration() {
		return new BrowserConfiguration(BrowserType.INTERNET_EXPLORER);
	}

	@Override
	long getTimeOutAfterReloadUrl() {
		return 400;
	}

	@Override
	long getTimeOutAfterEvent() {
		return 800;
	}

}
