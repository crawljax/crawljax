package com.crawljax.core.largetests;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assume.assumeThat;

import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.test.BrowserTest;

@Category(BrowserTest.class)
public class LargePhantomJSTest extends LargeTestBase {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		assumeThat(System.getProperty(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY),
		        notNullValue());
	}

	@Override
	BrowserConfiguration getBrowserConfiguration() {
		return new BrowserConfiguration(BrowserType.PHANTOMJS, 1);
	}

	@Override
	long getTimeOutAfterReloadUrl() {
		return 200;
	}

	@Override
	long getTimeOutAfterEvent() {
		return 200;
	}

}
