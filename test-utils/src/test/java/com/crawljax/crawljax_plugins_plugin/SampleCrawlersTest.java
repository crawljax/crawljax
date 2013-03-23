package com.crawljax.crawljax_plugins_plugin;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasEdges;
import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.junit.Assert.*;
import java.net.URL;

import org.junit.Test;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.crawltests.SimpleInputSiteCrawl;
import com.crawljax.crawltests.SimpleJsSiteCrawl;
import com.crawljax.crawltests.SimpleSiteCrawl;
import com.crawljax.crawltests.SimpleXpathCrawl;
import com.crawljax.test.BaseCrawler;
import com.crawljax.test.WebServer;

public class SampleCrawlersTest {
	private BaseCrawler crawler;
	private CrawlSession crawl;
	private int numStates;
	private int numEdges;
	
	private void setupSimpleCrawler() throws Exception {
		crawler = new SimpleSiteCrawl();
		crawl = crawler.crawl();
		numStates = SimpleSiteCrawl.NUMBER_OF_STATES;
		numEdges = SimpleSiteCrawl.NUMBER_OF_EDGES;
	}
	
	private void setupJsCrawler() throws Exception {
		crawler = new SimpleJsSiteCrawl();
		crawl = crawler.crawl();
		numStates = SimpleJsSiteCrawl.NUMBER_OF_STATES;
		numEdges = SimpleJsSiteCrawl.NUMBER_OF_EDGES;
	}
	
	private void setupInputCrawler() throws Exception {
		crawler = new SimpleInputSiteCrawl();
		crawl = crawler.crawl();
		numStates = SimpleInputSiteCrawl.NUMBER_OF_STATES;
		numEdges = SimpleInputSiteCrawl.NUMBER_OF_EDGES;
	}
	
	private void setupXpathCrawl() throws Exception {
		crawler = new SimpleXpathCrawl();
		crawl = crawler.crawl();
		numStates = SimpleInputSiteCrawl.NUMBER_OF_STATES;
		numEdges = SimpleInputSiteCrawl.NUMBER_OF_EDGES;
	}

	@Test
	public void testSimpleCrawlerFlowGraph() throws Exception {
		setupSimpleCrawler();
		runFlowGraphTest();
	}
	
	@Test
	public void testSimpleCrawlerUrl() throws Exception {
		setupSimpleCrawler();
		runUrlTest();
	}

	@Test
	public void testJsCrawlerFlowGraph() throws Exception {
		setupJsCrawler();
		runFlowGraphTest();
	}
	
	@Test
	public void testJsCrawlerUrl() throws Exception{
		setupJsCrawler();
		runUrlTest();
	}

	@Test
	public void testInputCrawlerFlowGraph() throws Exception {
		setupInputCrawler();
		runFlowGraphTest();
	}
	
	@Test
	public void testInputCrawlerUrl() throws Exception {
		setupInputCrawler();
		runUrlTest();
	}

	@Test
	public void testSimpleXPathCrawlFlowGrah() throws Exception {
		setupXpathCrawl();
		runFlowGraphTest();
	}
	
	@Test
	public void testSimpleXPathCrawlUrl() throws Exception {
		setupXpathCrawl();
		runUrlTest();
	}
	
	private void runFlowGraphTest() throws Exception {
		StateFlowGraph stateFlowGraph = crawl.getStateFlowGraph();
		assertThat(stateFlowGraph, hasStates(numStates));
		assertThat(stateFlowGraph, hasEdges(numEdges));
	}
	
	
	private void runUrlTest() throws Exception {
		WebServer server = crawler.getWebServer();
		URL site = new URL("http", "localhost", server.getPort(), "/");
		assertTrue(site.getPath().equals(server.getSiteUrl().getPath()));
	}

}
