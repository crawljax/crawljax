package com.crawljax.core.state.duplicatedetection;

/**
 * Exception thrown while generating features from a document *
 */
public class FeatureException extends Exception {
	
	private static final long serialVersionUID = 6215665321096676600L;

	FeatureException(String message) {
		super(message);
	}
}
