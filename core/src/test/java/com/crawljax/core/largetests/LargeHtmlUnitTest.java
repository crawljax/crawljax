package com.crawljax.core.largetests;

import org.junit.Ignore;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.BrowserConfiguration;

@Ignore("HTML Unit is not supported at the moment")
public class LargeHtmlUnitTest extends LargeTestBase {

	@Override
	BrowserConfiguration getBrowserConfiguration() {
		return new BrowserConfiguration(BrowserType.htmlunit);
	}

	@Override
	long getTimeOutAfterReloadUrl() {
		return 400;
	}

	@Override
	long getTimeOutAfterEvent() {
		return 400;
	}

}
