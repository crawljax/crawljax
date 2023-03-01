package com.crawljax.core.largetests;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.test.BrowserTest;
import org.junit.experimental.categories.Category;

@Category(BrowserTest.class)
public class LargeFirefoxTest extends LargeTestBase {

    @Override
    BrowserConfiguration getBrowserConfiguration() {
        return new BrowserConfiguration(BrowserType.FIREFOX_HEADLESS);
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
