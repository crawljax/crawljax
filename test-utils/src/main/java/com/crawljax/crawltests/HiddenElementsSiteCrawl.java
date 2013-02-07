package com.crawljax.crawltests;

import com.crawljax.test.BaseCrawler;

/**
 * Wraps a Crawljax instance the crawls the simplesite.
 */
public class HiddenElementsSiteCrawl extends BaseCrawler {

	public static final int NUMBER_OF_STATES = 3;

	public HiddenElementsSiteCrawl() {
		super("hidden-elements-site");
	}

}
