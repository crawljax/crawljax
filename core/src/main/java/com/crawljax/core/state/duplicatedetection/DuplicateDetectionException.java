package com.crawljax.core.state.duplicatedetection;

/**
 * Default exception thrown by NearDuplicateDetection-algorithms.
 */
@SuppressWarnings("serial")
public class DuplicateDetectionException extends RuntimeException {

	/**
	 * Default constructor for the exception.
	 * 
	 * @param message
	 *            messages provided along with the thrown exception.
	 */
	DuplicateDetectionException(String message) {
		super(message);
	}
}
