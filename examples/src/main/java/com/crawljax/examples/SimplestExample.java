package com.crawljax.examples;

import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.CrawljaxConfiguration;

/**
 * Crawls our demo site with the default configuration. The crawl will log what it's doing but will
 * not produce any output.
 */
public class SimplestExample {

	/**
	 * Run this method to start the crawl.
	 */
	public static void main(String[] args) {
		CrawljaxRunner crawljax =
		        new CrawljaxRunner(CrawljaxConfiguration.builderFor("http://demo.crawljax.com/")
		                .build());
		crawljax.call();
	}
}
