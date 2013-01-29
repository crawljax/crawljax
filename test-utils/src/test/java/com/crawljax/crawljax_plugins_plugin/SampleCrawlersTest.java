package com.crawljax.crawljax_plugins_plugin;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasEdges;
import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.crawltests.HiddenElementsSiteCrawl;
import com.crawljax.crawltests.SimpleInputSiteCrawl;
import com.crawljax.crawltests.SimpleJsSiteCrawl;
import com.crawljax.crawltests.SimpleSiteCrawl;
import com.crawljax.crawltests.SimpleXpathCrawl;

public class SampleCrawlersTest {

	@Test
	public void testSimpleCrawler() throws Exception {
		CrawlSession crawl = new SimpleSiteCrawl().crawl();
		StateFlowGraph stateFlowGraph = crawl.getStateFlowGraph();
		assertThat(stateFlowGraph, hasStates(SimpleSiteCrawl.NUMBER_OF_STATES));
		assertThat(stateFlowGraph, hasEdges(SimpleSiteCrawl.NUMBER_OF_EDGES));
	}

	@Test
	public void testJSCrawler() throws Exception {
		CrawlSession crawl = new SimpleJsSiteCrawl().crawl();
		StateFlowGraph stateFlowGraph = crawl.getStateFlowGraph();
		assertThat(stateFlowGraph, hasStates(SimpleJsSiteCrawl.NUMBER_OF_STATES));
		assertThat(stateFlowGraph, hasEdges(SimpleJsSiteCrawl.NUMBER_OF_EDGES));
	}

	@Test
	public void testInputCrawler() throws Exception {
		CrawlSession crawl = new SimpleInputSiteCrawl().crawl();
		StateFlowGraph stateFlowGraph = crawl.getStateFlowGraph();
		assertThat(stateFlowGraph, hasStates(SimpleInputSiteCrawl.NUMBER_OF_STATES));
		assertThat(stateFlowGraph, hasEdges(SimpleInputSiteCrawl.NUMBER_OF_EDGES));
	}

	/**
	 * Shows <a href='https://github.com/crawljax/crawljax/issues/97'>Issue 97</a>
	 */
	@Test
	@Ignore("Test that shows Issue #97")
	public void testHiddenElementsSiteCrawl() throws Exception {
		// new SimpleInputSiteCrawl().showWebSite();
		CrawlSession crawl = new HiddenElementsSiteCrawl().crawl();
		StateFlowGraph stateFlowGraph = crawl.getStateFlowGraph();
		assertThat(stateFlowGraph, hasStates(HiddenElementsSiteCrawl.NUMBER_OF_STATES));
	}

	@Test
	public void testSimpleXPathCrawl() throws Exception {
		CrawlSession crawl = new SimpleXpathCrawl().crawl();
		StateFlowGraph stateFlowGraph = crawl.getStateFlowGraph();
		assertThat(stateFlowGraph, hasStates(SimpleInputSiteCrawl.NUMBER_OF_STATES));
		assertThat(stateFlowGraph, hasEdges(SimpleInputSiteCrawl.NUMBER_OF_EDGES));
	}
}
