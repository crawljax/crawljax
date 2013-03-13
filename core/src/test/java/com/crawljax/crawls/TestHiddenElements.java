package com.crawljax.crawls;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.test.BaseCrawler;
import com.crawljax.test.BrowserTest;

public class TestHiddenElements implements BrowserTest {

	/**
	 * Shows <a href='https://github.com/crawljax/crawljax/issues/97'>Issue 97</a>
	 */
	@Test
	public void testHiddenElementsSiteCrawl() throws Exception {
		CrawlSession crawl = new BaseCrawler("hidden-elements-site") {
			@Override
			public CrawljaxConfigurationBuilder newCrawlConfiguartionBuilder() {
				CrawljaxConfigurationBuilder builder =
				        super.newCrawlConfiguartionBuilder();
				builder.crawlRules().crawlHiddenAnchors(true);
				return builder;
			}
		}.crawl();

		StateFlowGraph stateFlowGraph = crawl.getStateFlowGraph();
		/*
		 * TODO Fix issue #97 https://github.com/crawljax/crawljax/issues/97 It is now party hacked
		 * by following HREF links.
		 */
		int withIssue97 = 3 - 1;
		assertThat(stateFlowGraph, hasStates(withIssue97));
	}

	/**
	 * Shows <a href='https://github.com/crawljax/crawljax/issues/97'>Issue 97</a>
	 */
	@Test
	public void whenHiddenElementsOfItShouldntCrawl() throws Exception {
		CrawlSession crawl = new BaseCrawler("hidden-elements-site").crawl();
		StateFlowGraph stateFlowGraph = crawl.getStateFlowGraph();

		// this includes the bug of #97
		int expectedStates = 3 - 2;
		assertThat(stateFlowGraph, hasStates(expectedStates));
	}

}
