package com.crawljax.core.state.duplicatedetection;

import java.util.List;

/**
 * Interface for types of features, such as Shingles or outgoing/incoming links.
 */
public interface FeatureType {
	
	/**
	 * Generates a list of features (strings) for a given document
	 * @param doc the original (dom) document.
	 * @return a list of features
	 * @throws FeatureException is thrown when the implementation failed to generate features.
	 */
	List<String> getFeatures(String doc) throws FeatureException;

}
