package com.crawljax.crawltests;

import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.test.BaseCrawler;

/**
 * Wraps a Crawljax instance the crawls the simplesite.
 */
public class SimpleXpathCrawl extends BaseCrawler {

	public static final int NUMBER_OF_STATES = 3;
	public static final int NUMBER_OF_EDGES = 2;

	public SimpleXpathCrawl() {
		super("simple-xpath-site");
	}

	@Override
	public CrawlSpecification newCrawlSpecification() {
		CrawlSpecification spec = super.newCrawlSpecification();
		spec.click("a").underXPath("//A[@class='click']");
		spec.dontClickChildrenOf("div").withId("dontClick");
		return spec;
	}
}
