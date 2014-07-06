package com.crawljax.examples;

import java.util.ArrayList;
import java.util.List;

import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.state.NDDStateVertexFactory;
import com.crawljax.core.state.duplicatedetection.*;
import com.crawljax.core.state.duplicatedetection.FeatureShingles.ShingleType;
import com.crawljax.domcomparators.AttributesStripper;
import com.crawljax.domcomparators.DomStructureStripper;
import com.crawljax.domcomparators.HeadStripper;
import com.crawljax.domcomparators.RedundantWhiteSpaceStripper;
import com.crawljax.plugins.crawloverview.CrawlOverview;
import com.google.common.collect.ImmutableList;

/**
 * Crawls the demo site with a custom duplicate-detection. The crawl will produce output using the
 * {@link CrawlOverview} plugin.
 */
public class DuplicateDetectionExample {

	// Threshold that will be used during the crawl.
	private static final double THRESHOLD = 0.3;

	private static final String URL = "http://demo.crawljax.com/";

	public static void main(String[] args) {
		// Setup the custom near-duplicate detection.
		// We will use a feature of 2 WORDS to generate fingerprints.
		FeatureType feature = FeatureShingles.withSize(2, ShingleType.WORDS);
		List<FeatureType> features = new ArrayList<FeatureType>();
		features.add(feature);
		
		// Broder's algorithm for the fingerprint-generation;
		NearDuplicateDetection ndd =
		        new NearDuplicateDetectionBroder(THRESHOLD, ImmutableList.copyOf(features));

		// Setup the crawler with the ndd
		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(URL);
		// Use the statevertex-implementation which uses near-duplicate detection
		builder.setStateVertexFactory(new NDDStateVertexFactory());
		// Set a custom near-duplicate detection Factory.
		builder.setNearDuplicateDetectionFactory(ndd);

		// Use recommended strippers for the words-type
		builder.addDomStripper(new HeadStripper());
		builder.addDomStripper(new DomStructureStripper());
		builder.addDomStripper(new AttributesStripper());
		builder.addDomStripper(new RedundantWhiteSpaceStripper());
		
		// Run crawler
		CrawljaxRunner crawljax = new CrawljaxRunner(builder.build());
		crawljax.call();
	}
}
