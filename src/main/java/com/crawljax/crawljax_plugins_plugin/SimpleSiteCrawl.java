package com.crawljax.crawljax_plugins_plugin;

import org.eclipse.jetty.util.resource.Resource;

/**
 * Wraps a Crawljax instance the crawls the simplesite.
 */
public class SimpleSiteCrawl extends SampleCrawler {

	private static final Resource SIMEPLE_SITE = Resource.newClassPathResource("/simple-site");

	protected SimpleSiteCrawl() {
		super(SIMEPLE_SITE);
	}

}
