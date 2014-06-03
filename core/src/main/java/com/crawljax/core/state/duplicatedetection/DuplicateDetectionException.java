package com.crawljax.core.state.duplicatedetection;

/**
 * Exceptions thrown by NearDuplicateDetection-algorithms.
 */
@SuppressWarnings("serial")
public class DuplicateDetectionException extends RuntimeException {

	DuplicateDetectionException(String message) {
		super(message);
	}
}
