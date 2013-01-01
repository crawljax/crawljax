package com.crawljax.crawltests;

/**
 * Wraps a Crawljax instance the crawls the simplesite.
 */
public class SimpleSiteCrawl extends SampleCrawler {

	public static final int NUMBER_OF_STATES = 4;
	public static final int NUMBER_OF_EDGES = 3;

	public SimpleSiteCrawl() {
		super("simple-site");
	}

}
