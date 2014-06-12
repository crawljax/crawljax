package com.crawljax.core.state.duplicatedetection;

import java.util.List;

/**
 * NearDuplicateDetection is the main class of the duplicate-detection-package. Depending on
 * features, hashGenerator and a threshold, it is tasked to determine whether two hashes are
 * (near-)duplicates. To run the NearDuplicateDetection (NDD) classes correctly, the following
 * criteria should be met: 1. provide a threshold, which should adhere to the constraints of the
 * specific implementation of the NDD. 2. provide at least 1 featureType. This lets the NDD know how
 * to hash the relevant content. Usage: 1. Assume you have two documents (strings), which need to be
 * evaluated, docA and docB 2. Initialize a NearDuplicateDetection-implementation, and provide a
 * threshold and feature(s). 3. Use generateHash to calculate HashA and HashB for docA and docB 4. To
 * check if docA and docB are duplicates, run isNearDuplicate(hashA, hashB).
 */
public interface NearDuplicateDetection {

	/**
	 * Generates an hash for a given document, using the set of features.
	 * 
	 * @param doc
	 *            a string, which should be larger than the feature-types.
	 * @return the hash(es) generated from the document.
	 */
	public Fingerprint generateHash(String doc);

	public List<FeatureType> getFeatures();

	public void setThreshold(double threshold);

	public void setFeatures(List<FeatureType> features);

	public double getThreshold();
}
