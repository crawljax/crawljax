package com.crawljax.examples;

import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.state.StateFlowGraph.StateFlowGraphType;

/**
 * Crawls our demo site with the default configuration. The crawl will log what it's doing but will
 * not produce any output.
 */
public class SimplestExample {

	/**
	 * Run this method to start the crawl.
	 */
	public static void main(String[] args) {
		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor("http://demo.crawljax.com/");
		builder.setGraphType(StateFlowGraphType.DB_IN_RAM_PATH);
  
		CrawljaxRunner crawljax =
		        new CrawljaxRunner(builder.build());
		crawljax.call();
	}
}
