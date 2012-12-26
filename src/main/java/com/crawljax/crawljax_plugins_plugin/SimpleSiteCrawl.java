package com.crawljax.crawljax_plugins_plugin;

import org.eclipse.jetty.util.resource.Resource;

/**
 * Wraps a Crawljax instance the crawls the simplesite.
 */
public class SimpleSiteCrawl extends SampleCrawler {

	public static final int NUMBER_OF_STATES = 4;
	public static final int NUMBER_OF_EDGES = 3;

	private static final Resource SIMEPLE_SITE = Resource.newClassPathResource("/simple-site");

	protected SimpleSiteCrawl() {
		super(SIMEPLE_SITE);
	}

}
