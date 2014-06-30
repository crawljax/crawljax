package com.crawljax.core.state.duplicatedetection;

/**
 * An interface/adapter for different hash-functions (such as xxHash and Murmur3). In this context a
 * HashGenerator is used to determine the method how the features ({@link FeatureType} are hashed.
 */
public interface HashGenerator {

	/**
	 * Generate a hash for an input string.
	 * 
	 * @param input
	 *            the string to be hashed
	 * @return a hash, bits stored in an int representation.
	 */
	int generateHash(String input);
}
