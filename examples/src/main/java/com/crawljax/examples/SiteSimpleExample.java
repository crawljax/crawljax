package com.crawljax.examples;

import java.io.File;

import ca.ubc.eece310.groupL2C1.Specification_Metrics_Plugin;

import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.plugins.crawloverview.CrawlOverview;

/**
 * Crawls google.com in IE.
 */
public final class SiteSimpleExample {

	private static final String URL = "http://www.google.com";

	private static final String ALL_ANCHORS = "a";
	private static final String LANGUAGE_TOOLS = "Language Tools";

	private static final String HEADER_XPATH = "//DIV[@id='guser']";

	private static final int MAX_CRAWL_DEPTH = 7;
	private static final int MAX_STATES = 7;

	/**
	 * Entry point
	 */
	public static void main(String[] args) {
		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(URL);
		builder.crawlRules().clickDefaultElements();
		builder.crawlRules().dontClick(ALL_ANCHORS).underXPath(HEADER_XPATH);
		builder.crawlRules().dontClick(ALL_ANCHORS).withText(LANGUAGE_TOOLS);

		// limit the crawling scope
		builder.setMaximumStates(MAX_STATES);
		builder.setMaximumDepth(MAX_CRAWL_DEPTH);

		builder.crawlRules().setInputSpec(getInputSpecification());
		
		Specification_Metrics_Plugin SMP=new Specification_Metrics_Plugin();

		SMP.setOutputFolder("specification_metrics_output");
		builder.addPlugin(SMP);
		
		CrawljaxController crawljax = new CrawljaxController(builder.build());
		crawljax.run();
	}

	private static InputSpecification getInputSpecification() {
		InputSpecification input = new InputSpecification();

		// enter "Crawljax" in the search field
		input.field("q").setValue("Crawljax");
		return input;
	}

	private SiteSimpleExample() {
		// Utility class
	}
}
