package com.crawljax.core.state.duplicatedetection;

/**
 * Interface for the different factories building HashGenerators
 */
public interface HashGeneratorFactory {
	
	/**
	 * Get an instance of the HashGenerator-implementation.
	 */
	public HashGenerator getInstance();

}
