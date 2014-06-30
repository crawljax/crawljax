package com.crawljax.core.state.duplicatedetection;

import java.util.ArrayList;
import java.util.List;

import com.crawljax.core.state.duplicatedetection.FeatureShingles.SizeType;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * The Guice-module for the NearDuplicateDetection-package for Crawljax
 */
public class DuplicateDetectionModule extends AbstractModule {

	private NearDuplicateDetection nearDuplicateDetectionFactory;

	/**
	 * Constructor with a custom nearDuplicateDetection-instance, which will be used by Guice. If
	 * null, Guice will use the default near-duplicate detection.
	 * 
	 * @param factory
	 *            a NearDuplicateDetection, a hashgenator is not needed as it will be overriden by
	 *            Guice.
	 */
	public DuplicateDetectionModule(NearDuplicateDetection factory) {
		// Set defaults
		nearDuplicateDetectionFactory = factory;
	}

	/**
	 * Constructor for using the default NearDuplicateDetection.
	 */
	public DuplicateDetectionModule() {
	}

	/**
	 * Used by Guice
	 */
	@Override
	protected void configure() {
		// Type of Hash-generator
		bind(HashGenerator.class).to(XxHashGenerator.class);
	}

	/**
	 * Provider-method for the NearDuplicateDetection. If specified it will use the
	 * NearDuplicateDetection-instance supplied in the constructor.
	 * 
	 * @param hasher
	 *            Guice will supply the HashGenerator, which is binded in configure()
	 * @return a NearDuplicateDetection-instance
	 */
	@Provides
	NearDuplicateDetection provideNearDuplicateDetection(HashGenerator hasher) {
		double threshold = 1;
		if (nearDuplicateDetectionFactory == null) {
			List<FeatureType> features = new ArrayList<FeatureType>(1);
			features.add(FeatureShingles.withSize(3, SizeType.WORDS));
			nearDuplicateDetectionFactory =
			        new NearDuplicateDetectionCrawlhash(threshold, ImmutableList.copyOf(features));
		}
		nearDuplicateDetectionFactory.setHashGenerator(hasher);
		return nearDuplicateDetectionFactory;
	}
}
