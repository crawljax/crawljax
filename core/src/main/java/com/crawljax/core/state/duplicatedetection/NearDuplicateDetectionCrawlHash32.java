package com.crawljax.core.state.duplicatedetection;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class NearDuplicateDetectionCrawlHash32 implements NearDuplicateDetection {

	private static final int HASH_LENGTH = 32;
	private static final int HEX_ONE = 0x00000001;
	private static final int HEX_ZERO = 0x00000000;

	private final static float THRESHOLD_UPPERLIMIT = HASH_LENGTH;
	private final static float THRESHOLD_LOWERLIMIT = 0;
	
	private List<FeatureType> features;
	private double threshold;
	private HashGenerator hashGenerator;
	
	public NearDuplicateDetectionCrawlHash32(double threshold, List<FeatureType> fs, HashGenerator hg) {
		checkPreconditionsFeatures(fs);
		checkPreconditionsThreshold(threshold);
		this.hashGenerator = hg;
		this.features = fs;
		this.threshold = threshold;
	}
	
	/**
	 * This constructor uses the default, generally most optimal, values for the threshold and 
	 * features. It is also used for Guice-invocations.
	 * @param hg a hash-generator, such as the XxHashGenerator.
	 */
	@Inject
	public NearDuplicateDetectionCrawlHash32(HashGenerator hg) {
		// TODO should be done in the configuration?
		this.hashGenerator = hg;
		this.features = new ArrayList<FeatureType>();
		this.threshold = 3;
	}
	
	@Override
	public int[] generateHash(String doc) {
		checkPreconditionsFeatures(features);
		checkPreconditionsThreshold(threshold);
		int[] bits = new int[HASH_LENGTH];
		List<String> tokens = this.generateFeatures(doc);
		// loop through all tokens (ie shingles), calculate the hash, and add
		// the hash to the array.
		for (String t : tokens) {
			int v = hashGenerator.generateHash(t);
			bits = addHashToArray(v, bits);
		}
		int[] hashArray = {projectArrayOnHash(bits)};
		return hashArray;
	}

	private List<String> generateFeatures(String doc) {
		List<String> li = new ArrayList<String>();
		for(FeatureType feature : features) {
			li.addAll(feature.getFeatures(doc));
		}
		return li;
	}
	
	private int projectArrayOnHash(int[] bits) {
		int hash = HEX_ZERO;
		int one = HEX_ONE;
		for (int i = HASH_LENGTH; i >= 1; --i) {
			// for each int in bits, if the current position > 1, set bit to 1
			if (bits[i - 1] > 1) { 
				hash |= one;
			}
			// Move the bit position one bit to the left.
			one = one << 1;
		}
		return hash;
	}
	
	private int[] addHashToArray(int hash, int[] bits) {
		// Loop through each bit-position, starting with the least significant.
		for (int i = HASH_LENGTH; i >= 1; --i) {
			// check if bit-position is one, if so add 1 to array at current position.
			if (((hash >> (HASH_LENGTH - i)) & 1) == 1)
				++bits[i - 1];
			else
				--bits[i - 1];
		}
		return bits;
	}

	public int hammingDistance(int hash1, int hash2) {
		int i = hash1 ^ hash2;
		i = i - ((i >>> 1) & 0x55555555);
		i = (i & 0x33333333) + ((i >>> 2) & 0x33333333);
		i = (i + (i >>> 4)) & 0x0f0f0f0f;
		i = i + (i >>> 8);
		i = i + (i >>> 16);
		return i & 0x3f;
	}

	@Override
	public boolean isNearDuplicateHash(int[] hash1, int[] hash2) {
		return ((double) hammingDistance(hash1[0],hash2[0])) <= threshold;
	}
	
	/**
	 * Checks the precondition for the feature-list, which should not be empty or null.
	 * @param features feature-list to be checked
	 */
	private void checkPreconditionsFeatures(List<FeatureType> features) {
		if(features == null || features.isEmpty()) {
			throw new FeatureException("Invalid feature-list provided, feature-list cannot be null or empty. (Provided: " + features + ")");
		}		
	}

	/**'
	 * Checks the precondition for the threshold, which should be within the predefined upper and lower bounds.
	 * @param threshold
	 */
	private void checkPreconditionsThreshold(double threshold) {
		if(threshold > THRESHOLD_UPPERLIMIT || threshold < THRESHOLD_LOWERLIMIT) {
			throw new FeatureException("Invalid threshold value " + threshold + ", threshold as to "
					+ "be between " + THRESHOLD_LOWERLIMIT + " and " + THRESHOLD_UPPERLIMIT + ".");
		}
	}
	
	public double getThreshold() {
		return threshold;
	}

	@Override
	public double getDistance(int[] hash1, int[] hash2) {
		return hammingDistance(hash1[0], hash2[0]);
	}

	public List<FeatureType> getFeatures() {
		return features;
	}

	@Override
	public void setThreshold(double threshold) {
		checkPreconditionsThreshold(threshold);
		this.threshold = threshold;
		
	}

	public void setFeatures(List<FeatureType> features) {
		checkPreconditionsFeatures(features);
		this.features = features;
	}
}
