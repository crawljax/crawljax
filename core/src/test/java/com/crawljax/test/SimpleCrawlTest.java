package com.crawljax.test;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.forms.FormInputValueHelper;
import org.junit.Before;
import org.junit.Test;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasEdges;
import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.junit.Assert.assertThat;

public abstract class SimpleCrawlTest {

	private CrawlSession crawl;
	private final int NUMBER_OF_STATES;
	private final int NUMBER_OF_EDGES;

	public SimpleCrawlTest(int numberOfStates, int numberOfEdges) {
		this.NUMBER_OF_STATES = numberOfStates;
		this.NUMBER_OF_EDGES = numberOfEdges;
	}

	@Before
	public void setup() {
		/* Reset the singleton instance between tests. */
		FormInputValueHelper.reset();
	}

	@Test
	public void testInputCrawlerFlowGraph() throws Exception {
		BaseCrawler crawler = getCrawler();
		crawl = crawler.crawl();
		verifyGraphSize(NUMBER_OF_STATES, NUMBER_OF_EDGES);
	}

	public abstract BaseCrawler getCrawler();

	protected void verifyGraphSize(int numberOfStates, int numberOfEdges) {
		StateFlowGraph stateFlowGraph = crawl.getStateFlowGraph();
		assertThat(stateFlowGraph, hasStates(numberOfStates));
		assertThat(stateFlowGraph, hasEdges(numberOfEdges));
	}

}
