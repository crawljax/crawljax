package com.crawljax.external;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.crawljax.crawljax_plugins_plugin.SampleCrawlersTest;
import com.crawljax.test.BrowserTest;

@RunWith(Suite.class)
@SuiteClasses(SampleCrawlersTest.class)
@Category(BrowserTest.class)
public class ExternalTestSuite {

}
