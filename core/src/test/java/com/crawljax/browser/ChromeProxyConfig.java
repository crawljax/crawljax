package com.crawljax.browser;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.test.BaseCrawler;
import com.crawljax.test.BrowserTest;
import org.eclipse.jetty.util.resource.Resource;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

@Category(BrowserTest.class)
public class ChromeProxyConfig {

	@Test
	public void chromeProxyConfig() {
		CrawlSession crawl =
				new BaseCrawler(Resource.newClassPathResource("/site"),
						"simplelink/simplelink.html") {
					@Override
					public CrawljaxConfigurationBuilder newCrawlConfigurationBuilder() {
						return super.newCrawlConfigurationBuilder();
					}
				}.crawl();

		assumeThat(System.getProperty("webdriver.chrome.driver"), is(notNullValue()));
		assertThat(crawl.getStateFlowGraph(), hasStates(2));
	}
}
