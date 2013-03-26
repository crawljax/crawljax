package com.crawljax.examples;

import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.configuration.InputSpecification;

public class SimplePopUpCancelExample
{
	private static final String URL0 = "http://gkh501.com/Download.html";
	private static final String URL1 = "http://courses.ece.ubc.ca/315/luisindex.html";

	private static final String ALL_ANCHORS = "a";
	private static final String LANGUAGE_TOOLS = "Language Tools";

	private static final String HEADER_XPATH = "//DIV[@id='guser']";

	private static final int MAX_CRAWL_DEPTH = 1;
	private static final int MAX_STATES = 10;

	/**
	 * Entry point
	 */
	public static void main(String[] args) {
		
		CrawljaxConfigurationBuilder builder = buildCrawlJax(URL0);
		CrawljaxController crawljax = new CrawljaxController(builder.build());
		crawljax.run();
		
		builder = buildCrawlJax(URL1);
		crawljax = new CrawljaxController(builder.build());

		crawljax.run();
	}
	
	private static CrawljaxConfigurationBuilder buildCrawlJax(String URL)
	{
		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(URL);

		builder.crawlRules().clickDefaultElements();
		builder.crawlRules().dontClick(ALL_ANCHORS).underXPath(HEADER_XPATH);
		builder.crawlRules().dontClick(ALL_ANCHORS).withText(LANGUAGE_TOOLS);
		// limit the crawling scope
		//builder.crawlRules().setCancelPopUps("NONE");
		//builder.crawlRules().setPopUpTimer(5000);
		builder.setMaximumStates(MAX_STATES);
		builder.setMaximumDepth(MAX_CRAWL_DEPTH);
		builder.crawlRules().setInputSpec(getInputSpecification());
		return builder;
	}
	
	private static InputSpecification getInputSpecification() {
		InputSpecification input = new InputSpecification();

		// enter "Crawljax" in the search field
		input.field("q").setValue("Crawljax");
		return input;
	}

}