package com.crawljax.core.state.duplicatedetection;

/**
 * Exception thrown while generating features from a document *
 */
@SuppressWarnings("serial")
public class FeatureException extends RuntimeException {
	

	FeatureException(String message) {
		super(message);
	}
}
