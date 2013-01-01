package com.crawljax.crawljax_plugins_plugin;

/**
 * Wraps a Crawljax instance the crawls the simplesite.
 */
public class SimpleJsSiteCrawl extends SampleCrawler {

	public static final int NUMBER_OF_STATES = 11;
	public static final int NUMBER_OF_EDGES = 10;

	public SimpleJsSiteCrawl() {
		super("simple-js-site");
	}

	@Override
	public void setup() throws Exception {
		super.setup();
		getCrawlSpec().setDepth(4);
	}

}
