package com.crawljax.core;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

import com.crawljax.condition.NotXPathCondition;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertex;

/**
 * Test class for limiting the max states per URL 
 */
public class MaxOutgoingIndexTest {

	private static final int MAX_STATES_PER_URL = 3;
	private static final long WAIT_TIME_AFTER_EVENT = 200;
	private static final long WAIT_TIME_AFTER_RELOAD = 20;
	private static final String URL = "http://spci.st.ewi.tudelft.nl/demo/crawljax/";
	
	@Test
	public void testMaxStatesPerUrl() throws CrawljaxException, ConfigurationException, InterruptedException {
		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(URL);
		builder.crawlRules().insertRandomDataInInputForms(false);
		
		builder.setMaximumStatesPerUrl(MAX_STATES_PER_URL);
		
		builder.crawlRules().click("a");

		// click these elements
		builder.crawlRules().clickDefaultElements();
		builder.crawlRules().click("div").withAttribute("class", "clickable");

		// but don't click these
		builder.crawlRules().dontClick("a").withAttribute("class", "ignore");
		builder.crawlRules().dontClick("a").underXPath("//DIV[@id='footer']");

		// Set timeouts
		builder.crawlRules().waitAfterReloadUrl(WAIT_TIME_AFTER_RELOAD, TimeUnit.MILLISECONDS);
		builder.crawlRules().waitAfterEvent(WAIT_TIME_AFTER_EVENT, TimeUnit.MILLISECONDS);

		// Add a condition that this XPath doesn't exits
		builder.crawlRules().addCrawlCondition("No spans with foo as class",
		        new NotXPathCondition(
		                "//*[@class='foo']"));

		CrawljaxController crawljax = new CrawljaxController(builder.build());
		
		crawljax.run();
		
		StateFlowGraph graph = crawljax.getSession().getStateFlowGraph();

		Object[] vertices = graph.getAllStates().toArray();
		for(int i = 0; i < graph.getAllStates().size(); i++) {
			assertTrue(graph.getOutgoingStates((StateVertex) vertices[i]).size() <= MAX_STATES_PER_URL);
		}
	}
	
}
