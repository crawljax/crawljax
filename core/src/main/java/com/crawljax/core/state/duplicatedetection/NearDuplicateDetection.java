package com.crawljax.core.state.duplicatedetection;

/**
 * Settings:
 * - Features
 * - Feature weights
 * - Threshold
 *
 */
public interface NearDuplicateDetection {
	
	public int generateHash(String doc) throws FeatureShinglesException;
	
	public boolean hasNearDuplicateHash(int hash); // Threshold defined in settings?
	
	public int findNearDuplicateHash(int hash); // Threshold defined internal or in settings

	public boolean isNearDuplicateHash(int hash1, int hash2);
		
	public int getDistance(int hash1, int hash2);
}
