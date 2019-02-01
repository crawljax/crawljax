package com.crawljax.core.largetests;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.test.BrowserTest;
import org.junit.experimental.categories.Category;

@Category(BrowserTest.class)
public class LargeChromeTest extends LargeTestBase {

	@Override
	BrowserConfiguration getBrowserConfiguration() {
		return new BrowserConfiguration(BrowserType.CHROME_HEADLESS);
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
