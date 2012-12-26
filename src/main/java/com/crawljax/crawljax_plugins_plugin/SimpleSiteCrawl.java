package com.crawljax.crawljax_plugins_plugin;

/**
 * Wraps a Crawljax instance the crawls the simplesite.
 */
public class SimpleSiteCrawl extends SampleCrawler {

	public static final int NUMBER_OF_STATES = 4;
	public static final int NUMBER_OF_EDGES = 3;

	protected SimpleSiteCrawl() {
		super("simple-site");
	}

}
