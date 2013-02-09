package com.crawljax.crawltests;

import com.crawljax.test.BaseCrawler;

/**
 * Wraps a Crawljax instance the crawls the simplesite.
 */
public class SimpleJsSiteCrawl extends BaseCrawler {

	public static final int NUMBER_OF_STATES = 11;
	public static final int NUMBER_OF_EDGES = 10;

	public SimpleJsSiteCrawl() {
		super("simple-js-site");
	}

}
