package com.crawljax.crawltests;

import org.eclipse.jetty.util.resource.Resource;

import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.test.BaseCrawler;

/**
 * Wraps a Crawljax instance the crawls the simplesite.
 */
public class SimpleXpathCrawl extends BaseCrawler {

	public static final int NUMBER_OF_STATES = 3;
	public static final int NUMBER_OF_EDGES = 2;

	public SimpleXpathCrawl() {
		super(Resource.newClassPathResource("sites"), "simple-xpath-site");
	}

	@Override
	protected CrawljaxConfigurationBuilder newCrawlConfigurationBuilder() {
		CrawljaxConfigurationBuilder builder = super.newCrawlConfigurationBuilder();
		builder.crawlRules().click("a").underXPath("//A[@class='click']");
		builder.crawlRules().dontClickChildrenOf("div").withId("dontClick");
		return builder;
	}

}
