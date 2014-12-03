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
import com.crawljax.test.BaseCrawler;

public class SampleCrawlersTest {
	private BaseCrawler crawler;
	private CrawlSession crawl;

	@Test
	public void testSimpleCrawlerFlowGraph() throws Exception {
		crawler = new SimpleSiteCrawl();
		crawl = crawler.crawl();
		verifyGraphSize(SimpleSiteCrawl.NUMBER_OF_STATES, SimpleSiteCrawl.NUMBER_OF_EDGES);
	}

	@Test
	public void testJsCrawlerFlowGraph() throws Exception {
		crawler = new SimpleJsSiteCrawl();
		crawl = crawler.crawl();
		verifyGraphSize(SimpleJsSiteCrawl.NUMBER_OF_STATES, SimpleJsSiteCrawl.NUMBER_OF_EDGES);
	}

	@Test
	public void testInputCrawlerFlowGraph() throws Exception {
		crawler = new SimpleInputSiteCrawl();
		crawl = crawler.crawl();
		verifyGraphSize(SimpleInputSiteCrawl.NUMBER_OF_STATES,
		        SimpleInputSiteCrawl.NUMBER_OF_EDGES);
	}

	@Test
	public void testSimpleXPathCrawlFlowGrah() throws Exception {
		crawler = new SimpleXpathCrawl();
		crawl = crawler.crawl();
		verifyGraphSize(SimpleInputSiteCrawl.NUMBER_OF_STATES,
		        SimpleInputSiteCrawl.NUMBER_OF_EDGES);
	}

	private void verifyGraphSize(int numberOfStates, int numberOfEdges) throws Exception {
		StateFlowGraph stateFlowGraph = crawl.getStateFlowGraph();
		assertThat(stateFlowGraph, hasStates(numberOfStates));
		assertThat(stateFlowGraph, hasEdges(numberOfEdges));
	}
}
