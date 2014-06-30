package com.crawljax.core.state.duplicatedetection;

import java.util.List;

/**
 * A FeatureType, such as Shingles or outgoing/incoming links, is used in the process of generating
 * a hash for a {@link Fingerprint}. It splits a String into an array of so-called <i>features</i>,
 * which are basically short strings. These strings can be about anything, as long as they are
 * relevant to the fingerprint comparison. After the features have been created, a
 * {@link HashGenerator} will generate hashes of them.
 */
public interface FeatureType {

	/**
	 * Generates a list of features (strings) for a given document
	 * 
	 * @param doc
	 *            the original (dom) document.
	 * @return a list of features
	 * @throws FeatureException
	 *             is thrown when the implementation failed to generate features.
	 */
	List<String> getFeatures(String doc) throws FeatureException;

}
