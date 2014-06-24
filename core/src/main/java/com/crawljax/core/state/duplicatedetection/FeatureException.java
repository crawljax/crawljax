package com.crawljax.core.state.duplicatedetection;

/**
 * Exception thrown while generating features from a document *
 */
@SuppressWarnings("serial")
public class FeatureException extends DuplicateDetectionException {

	/**
	 * Default constructor for the exception.
	 * 
	 * @param message
	 *            messages provided along with the thrown exception.
	 */
	FeatureException(String message) {
		super(message);
	}
}
