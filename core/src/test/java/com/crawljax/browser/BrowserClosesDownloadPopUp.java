package com.crawljax.browser;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.junit.Assert.assertThat;

import org.eclipse.jetty.util.resource.Resource;
import org.junit.Test;

import com.crawljax.core.CrawlSession;
import com.crawljax.test.BaseCrawler;
import com.crawljax.test.BrowserTest;
import org.junit.experimental.categories.Category;

@Category(BrowserTest.class)
public class BrowserClosesDownloadPopUp {

	@Test
	public void webBrowserWindowOpensItIsIgnored() {
		BaseCrawler crawler =
		        new BaseCrawler(Resource.newClassPathResource("/site"), "download/download.html");
		CrawlSession crawl = crawler.crawl();
		assertThat(crawl.getStateFlowGraph(), hasStates(2));
	}
}
