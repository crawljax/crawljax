package com.crawljax.core.state.duplicatedetection;

import com.google.inject.AbstractModule;

/**
 * The Guice-module for the NearDuplicateDetection-package for Crawljax
 */
public class DuplicateDetectionModule extends AbstractModule {

	@Override
	protected void configure() {
		// Type of Hashgenerator
	    bind(HashGenerator.class).to(XxHashGenerator.class);
	    
	    // Type of Near-duplicate detection algorithm
	    // TODO make configurable through CrawljaxConfiguration?
	    bind(NearDuplicateDetection.class).to(NearDuplicateDetectionCrawlHash32.class);
	}
}
