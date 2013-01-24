package com.crawljax.crawltests;

import com.crawljax.core.configuration.CrawlSpecification;

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
		spec.dontClick("a").underXPath("//A[@class='noclick']");
		spec.click("a").underXPath("//A[@class='click']");
		return spec;
	}
}
