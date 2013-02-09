package com.crawljax.crawls;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;

import com.crawljax.core.CrawlSession;

public class TestCrawls {

	@Test
	@Ignore("Not sure if this is supposed to work")
	public void runCrawlExclusionTest() throws Exception {
		CrawlExclusionTest crawlExclusionTest = new CrawlExclusionTest();
		// crawlExclusionTest.showWebSite();
		CrawlSession crawl = crawlExclusionTest.crawl();
		assertThat(crawl.getStateFlowGraph(), hasStates(1));

	}

}
