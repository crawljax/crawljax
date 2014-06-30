package com.crawljax.core.state.duplicatedetection;

import com.google.common.collect.ImmutableCollection;

/**
 * <h3>About</h3>
 * <p>
 * NearDuplicateDetection is the main class of the duplicate-detection-package. Depending on
 * features, hashGenerator and a threshold, it is tasked to generate Fingerprints of a String. To
 * run the NearDuplicateDetection (NDD) classes correctly, the following criteria should be met:
 * </p>
 * <ol>
 * <li>provide a threshold, which should adhere to the constraints of the specific implementation of
 * the NDD.</li>
 * <li>provide at least 1 featureType. This lets the NDD know how to hash the relevant content.</li>
 * </ol>
 * <h3>Usage:</h3>
 * <ol>
 * <li>Crawljax: Decide on DOM Strippers. The stripped DOM is used in the fingerprint-generation</li>
 * <li>Create a list of FeatureType-implementation objects. These features determine how the
 * document is hashed into a fingerprint. For more info see {@link FeatureType}.</li>
 * <li>Decide on a threshold. A higher threshold will make it more likely that two states are viewed
 * as duplicates.</li>
 * <li>Optionally, use specific HashFactory, which is responsible for generating a hash from the
 * elements that the FeatureType-implementations provide.
 * <li>Initialize a NearDuplicateDetection-implementation, and provide a threshold and features.</li>
 * <li>Use <b>generateFingerprint</b> to calculate FingerprintA and FingerprintB for docA and docB 4
 * </li>
 * <li>To check if docA and docB are duplicates, see {@link Fingerprint}</li>
 * </ol>
 * 
 * <h3>Differences Algorithms</h3>
 * <p>
 * Two algorithms are provided, namely Broder and Crawlhash. Crawlhash is an efficient
 * implementation, which generatates only one 32 bit hash. This reduces the overhead and processing
 * time. Broder, on the other hand, generates a set of hashes for a single state. This makes it
 * slower than Crawlhash, but it provides more consistent and accurate results.
 * </p>
 * 
 * <h3>Example:</h3>
 * <p>
 * Objective tests have shown that a following configuration provides good results when comparing
 * textual content. (Make sure that only textual content is inputted)
 * </p>
 * <ul>
 * <li>NearDuplicateDetectionBroder</li>
 * <li>Features: [3 WORDS FeatureShingle]
 * <li>Threshold: 0.2</li>
 * <li>HashGenerator: XxHashGenerator</li>
 * </ul>
 */
public interface NearDuplicateDetection {

	/**
	 * Generates an hash for a given document, using the set of features.
	 * 
	 * @param doc
	 *            a string, which should be larger than the feature-types.
	 * @return the hash(es) generated from the document.
	 */
	Fingerprint generateFingerprint(String doc);

	/**
	 * Get the features used in the hash generation
	 * 
	 * @return list of features
	 */
	ImmutableCollection<FeatureType> getFeatures();

	/**
	 * Set new features to be used in fingerprint generation
	 * 
	 * @param features
	 *            a list of features with at least 1 feature
	 */
	void setFeatures(ImmutableCollection<FeatureType> features);

	/**
	 * Set new default threshold. Has to be within constraint of the implementation.
	 * 
	 * @param threshold
	 *            new default threshold.
	 */
	void setDefaultThreshold(double threshold);

	/**
	 * get the current default threshold
	 * 
	 * @return the default threshold
	 */
	double getDefaultThreshold();

	/**
	 * Sets a new hashGenerator.
	 * 
	 * @param hashGenerator
	 *            a HashGenerator-implementation
	 */
	void setHashGenerator(HashGenerator hashGenerator);
}
