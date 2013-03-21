package com.crawljax.browser;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.junit.Assert.assertThat;

import org.eclipse.jetty.util.resource.Resource;
import org.junit.Test;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.test.BrowserConfigCrawler;
import com.crawljax.test.BrowserTest;

public class ChromeProxyConfig implements BrowserTest {

	@Test
	public void chromeProxyConfig() {
		BrowserConfigCrawler crawler =
		        new BrowserConfigCrawler(Resource.newClassPathResource("/site"), "simplelink/simplelink.html").withBrowserConfig(new BrowserConfiguration(BrowserType.chrome));
		CrawlSession crawl = crawler.crawl();
		assertThat(crawl.getStateFlowGraph(), hasStates(2));
	}
}
