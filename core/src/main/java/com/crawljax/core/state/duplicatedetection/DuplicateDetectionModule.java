package com.crawljax.core.state.duplicatedetection;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * The Guice-module for the NearDuplicateDetection-package for Crawljax
 */
public class DuplicateDetectionModule extends AbstractModule {
	
	private NearDuplicateDetection nearDuplicateDetectionFactory;

	public DuplicateDetectionModule(NearDuplicateDetection factory) {
		// Set defaults
		this.nearDuplicateDetectionFactory = factory;
	}

	@Override
	protected void configure() {
		// Type of Hash-generator
		bind(HashGenerator.class).to(XxHashGenerator.class);
	}

	@Provides
	NearDuplicateDetection provideNearDuplicateDetection(HashGenerator hasher) {
		double threshold = 1;
		if(this.nearDuplicateDetectionFactory == null) {
			List<FeatureType> features = new ArrayList<FeatureType>();
			features.add(new FeatureShingles(3, FeatureShingles.SizeType.WORDS));
			nearDuplicateDetectionFactory = new NearDuplicateDetectionCrawlhash(threshold, features);
		}
		nearDuplicateDetectionFactory.setHashGenerator(hasher);
		return nearDuplicateDetectionFactory;
	}
}
