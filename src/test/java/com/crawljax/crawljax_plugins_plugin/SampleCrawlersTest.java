package com.crawljax.crawljax_plugins_plugin;

import static com.crawljax.matchers.StateFlowGraphMatches.hasEdges;
import static com.crawljax.matchers.StateFlowGraphMatches.hasStates;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.state.StateFlowGraph;

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
}
