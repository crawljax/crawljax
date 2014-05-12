package com.crawljax.core.state.duplicatedetection;

/**
 * Settings:
 * - Features
 * - Feature weights
 * - Threshold
 *
 */
public interface NearDuplicateDetection {
	
	public long generateHash(String doc);
	
	public boolean hasNearDuplicateHash(long hash); // Threshold defined in settings?

	public boolean hasNearDuplicateHash(long hash1, long hash2); // Threshold defined in settings?
	
	public long findNearDuplicateHash(long hash); // Threshold defined internal or in settings
}
