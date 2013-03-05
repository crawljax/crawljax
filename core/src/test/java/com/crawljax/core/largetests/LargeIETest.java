package com.crawljax.core.largetests;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assume.assumeThat;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.test.BrowserTest;

@Category(BrowserTest.class)
public class LargeIETest extends LargeTestBase {

	@BeforeClass
	public static void setUpBeforeClass() throws ConfigurationException, CrawljaxException {
		assumeThat(System.getProperty("os.name").toLowerCase(), containsString("windows"));
	}

	@Override
	BrowserConfiguration getBrowserConfiguration() {
		return new BrowserConfiguration(BrowserType.ie);
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
