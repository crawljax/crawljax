package com.crawljax.browser;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.junit.Assert.assertThat;

import org.eclipse.jetty.util.resource.Resource;
import org.junit.Test;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.test.BaseCrawler;
import com.crawljax.test.BrowserTest;

public class ChromeProxyConfig implements BrowserTest {

	@Test
	public void chromeProxyConfig() {
		CrawlSession crawl =
		        new BaseCrawler(Resource.newClassPathResource("/site"),
		                "simplelink/simplelink.html") {
			        @Override
			        public CrawljaxConfigurationBuilder newCrawlConfigurationBuilder() {
				        CrawljaxConfigurationBuilder builder =
				                super.newCrawlConfigurationBuilder();
				        builder.setBrowserConfig(new BrowserConfiguration(BrowserType.chrome));
				        return builder;
			        }
		        }.crawl();
		assertThat(crawl.getStateFlowGraph(), hasStates(2));
	}
}
