package com.crawljax.core.state.duplicatedetection;

import java.util.List;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * The Guice-module for the NearDuplicateDetection-package for Crawljax
 */
public class DuplicateDetectionModule extends AbstractModule {

	private double threshold;
	private List<FeatureType> fs;

	public DuplicateDetectionModule(double threshold, List<FeatureType> fs) {
		// Set defaults
		this.threshold = threshold;
		this.fs = fs;
	}

	@Override
	protected void configure() {
		// Type of Hash-generator
		bind(HashGenerator.class).to(XxHashGenerator.class);
	}

	@Provides
	NearDuplicateDetection provideNearDuplicateDetection(HashGenerator hasher) {
		return new NearDuplicateDetectionCrawlhash(this.threshold, this.fs, hasher);
	}
}
