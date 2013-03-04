package com.crawljax.crawls;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.state.StateFlowGraph;

public class TestCrawls {

	@Test
	@Ignore("Not sure if this is supposed to work")
	public void runCrawlExclusionTest() throws Exception {
		CrawlExclusionTest crawlExclusionTest = new CrawlExclusionTest();
		// crawlExclusionTest.showWebSite();
		CrawlSession crawl = crawlExclusionTest.crawl();
		assertThat(crawl.getStateFlowGraph(), hasStates(1));

	}

	/**
	 * Shows <a href='https://github.com/crawljax/crawljax/issues/97'>Issue 97</a>
	 */
	@Test
	public void testHiddenElementsSiteCrawl() throws Exception {
		// new SimpleInputSiteCrawl().showWebSite();
		CrawlSession crawl = new HiddenElementsSiteCrawl(true).crawl();
		StateFlowGraph stateFlowGraph = crawl.getStateFlowGraph();
		/*
		 * TODO Fix issue #97 https://github.com/crawljax/crawljax/issues/97 It is now party hacked
		 * by following HREF links.
		 */
		int withIssue97 = HiddenElementsSiteCrawl.NUMBER_OF_STATES - 1;
		assertThat(stateFlowGraph, hasStates(withIssue97));
	}

	/**
	 * Shows <a href='https://github.com/crawljax/crawljax/issues/97'>Issue 97</a>
	 */
	@Test
	public void whenHiddenElementsOfItShouldntCrawl() throws Exception {
		// new SimpleInputSiteCrawl().showWebSite();
		CrawlSession crawl = new HiddenElementsSiteCrawl(false).crawl();
		StateFlowGraph stateFlowGraph = crawl.getStateFlowGraph();

		// this includes the bug of #97
		int expectedStates = HiddenElementsSiteCrawl.NUMBER_OF_STATES - 2;
		assertThat(stateFlowGraph, hasStates(expectedStates));
	}

}
