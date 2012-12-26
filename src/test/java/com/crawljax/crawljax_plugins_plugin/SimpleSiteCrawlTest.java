package com.crawljax.crawljax_plugins_plugin;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.state.StateFlowGraph;

public class SimpleSiteCrawlTest {

	@Test
	public void test() throws Exception {
		CrawlSession crawl = new SimpleSiteCrawl().crawl();
		StateFlowGraph stateFlowGraph = crawl.getStateFlowGraph();
		assertThat(stateFlowGraph.getAllStates().size(), is(SimpleSiteCrawl.NUMBER_OF_STATES));
		assertThat(stateFlowGraph.getAllEdges().size(), is(SimpleSiteCrawl.NUMBER_OF_EDGES));
	}

}
