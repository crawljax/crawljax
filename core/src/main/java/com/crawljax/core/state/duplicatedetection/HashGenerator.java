package com.crawljax.core.state.duplicatedetection;

/**
 * An interface/adapter for different hash-functions (such as xxHash and Murmur3)
 */
public interface HashGenerator {
	
	/**
	 * Generate a hash for an input string.
	 * @param input the string to be hashed
	 * @return a hash, bits stored in an int representation.
	 */
	public int generateHash(String input);
}
