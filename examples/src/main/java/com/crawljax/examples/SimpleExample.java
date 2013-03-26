package com.crawljax.examples;

import org.apache.commons.configuration.ConfigurationException;

import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.configuration.InputSpecification;


/**
 * L2A2's group simple example
 */
public final class SimpleExample{
	
	private static final String URL = "http://www.google.com";
	private static final int MAX_DEPTH = 2;
	private static final int MAX_NUMBER_STATES = 8;
	
	/**
	 * Entry point
	 */
	public static void main(String[] args) throws ConfigurationException {
		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(URL);
		builder.crawlRules().insertRandomDataInInputForms(false);
		builder.alsoCrawl("http://www.bing.com");
		
		builder.crawlRules().click("a");
		builder.crawlRules().click("button");
		
		// except these
		builder.crawlRules().dontClick("a").underXPath("//DIV[@id='guser']");
		builder.crawlRules().dontClick("a").withText("Language Tools");
		
		// limit the crawling scope
		builder.setMaximumDepth(MAX_DEPTH);
		builder.setMaximumStates(MAX_NUMBER_STATES);
		
		builder.crawlRules().setInputSpec(getInputSpecification());
		
		CrawljaxController crawljax = new CrawljaxController(builder.build());
		crawljax.run();
		
	}
	
	private static InputSpecification getInputSpecification(){
		InputSpecification input = new InputSpecification();
		input.field("gbqfq").setValue("Crawljax");
		return input;
	}
}