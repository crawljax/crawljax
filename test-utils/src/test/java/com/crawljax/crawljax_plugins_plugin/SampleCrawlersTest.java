package com.crawljax.crawljax_plugins_plugin;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasEdges;
import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.state.StateFlowGraph;
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

	@Test
	public void testSimpleXPathCrawl() throws Exception {
		// new SimpleXpathCrawl().showWebSite();
		CrawlSession crawl = new SimpleXpathCrawl().crawl();
		StateFlowGraph stateFlowGraph = crawl.getStateFlowGraph();
		assertThat(stateFlowGraph, hasStates(SimpleInputSiteCrawl.NUMBER_OF_STATES));
		assertThat(stateFlowGraph, hasEdges(SimpleInputSiteCrawl.NUMBER_OF_EDGES));
	}
}
