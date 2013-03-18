package com.crawljax.crawltests;

import org.eclipse.jetty.util.resource.Resource;

import com.crawljax.test.BaseCrawler;

/**
 * Wraps a Crawljax instance the crawls the simplesite.
 */
public class SimpleSiteCrawl extends BaseCrawler {

	public static final int NUMBER_OF_STATES = 4;
	public static final int NUMBER_OF_EDGES = 5;

	public SimpleSiteCrawl() {
		super(Resource.newClassPathResource("sites"), "simple-site");
	}
}
