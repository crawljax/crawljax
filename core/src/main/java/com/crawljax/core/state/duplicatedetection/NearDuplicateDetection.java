package com.crawljax.core.state.duplicatedetection;

import java.util.List;

/**
 * Settings:
 * - Features
 * - Feature weights
 * - Threshold
 *
 */
public interface NearDuplicateDetection {
	
	public int[] generateHash(String doc) throws FeatureException;

	public boolean isNearDuplicateHash(int[] hash1, int[] hash2);
		
	public double getDistance(int[] hash1, int[] hash2);
	
	public List<FeatureType> getFeatures();
	
	public int getThreshold();
}
