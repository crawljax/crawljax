package com.crawljax.core.state.duplicatedetection;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * A Simhash-inspired algorithm for detecting near-duplicates. The idea is that all the features are
 * hashed to 32-bit hashes. The space of the hashes is projected on a 1-dimensional hash. Example:
 * {1011,1100,1001} -> 1001
 */
@Singleton
public class NearDuplicateDetectionCrawlhash implements NearDuplicateDetection {

	private static final int HASH_LENGTH = 32;
	private static final int HEX_ONE = 0x00000001;
	private static final int HEX_ZERO = 0x00000000;

	private final static float THRESHOLD_UPPERLIMIT = HASH_LENGTH;
	private final static float THRESHOLD_LOWERLIMIT = 0;

	private List<FeatureType> features;
	private double defaultThreshold;
	private HashGenerator hashGenerator;
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(NearDuplicateDetectionCrawlhash.class);

	@Inject
	public NearDuplicateDetectionCrawlhash(double threshold, List<FeatureType> fs,
	        HashGenerator hg) {
		checkPreconditionsFeatures(fs);
		checkPreconditionsThreshold(threshold);
		this.hashGenerator = hg;
		this.features = fs;
		this.defaultThreshold = threshold;
		LOG.info("NearDuplicateDetectionCrawlhash[defaultThreshold=" + threshold + ", feature-list = " + fs + ", HashGenerator= " + hg +"]");
	}

	@Override
	public Fingerprint generateFingerprint(String doc) {
		checkPreconditionsFeatures(features);
		checkPreconditionsThreshold(defaultThreshold);
		int[] bits = new int[HASH_LENGTH];
		List<String> tokens = this.generateFeatures(doc);
		// loop through all tokens (ie shingles), calculate the hash, and add
		// the hash to the array.
		for (String t : tokens) {
			int v = hashGenerator.generateHash(t);
			bits = addHashToArray(v, bits);
		}
		return new CrawlhashFingerprint(projectArrayOnHash(bits), defaultThreshold);
	}

	private List<String> generateFeatures(String doc) {
		List<String> li = new ArrayList<String>();
		for (FeatureType feature : features) {
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
	/**
	 * Checks the precondition for the feature-list, which should not be empty or null.
	 * 
	 * @param features
	 *            feature-list to be checked
	 */
	private void checkPreconditionsFeatures(List<FeatureType> features) {
		if (features == null || features.isEmpty()) {
			throw new DuplicateDetectionException(
			        "Invalid feature-list provided, feature-list cannot be "
			                + "null or empty. (Provided: " + features + ")");
		}
	}

	/**
	 * Checks the precondition for the defaultThreshold, which should be within the predefined upper and
	 * lower bounds.
	 * 
	 * @param defaultThreshold
	 */
	private void checkPreconditionsThreshold(double threshold) {
		if (threshold > THRESHOLD_UPPERLIMIT || threshold < THRESHOLD_LOWERLIMIT) {
			throw new DuplicateDetectionException("Invalid defaultThreshold value " + threshold
			        + ", defaultThreshold as to be between " + THRESHOLD_LOWERLIMIT + " and "
			        + THRESHOLD_UPPERLIMIT + ".");
		}
	}
	
	public double getDefaultThreshold() {
		return defaultThreshold;
	}
	
	public List<FeatureType> getFeatures() {
		return features;
	}

	public void setDefaultThreshold(double threshold) {
		checkPreconditionsThreshold(threshold);
		LOG.info("Threshold changed from {} to {}", this.defaultThreshold, threshold);
		this.defaultThreshold = threshold;

	}

	public void setFeatures(List<FeatureType> features) {
		checkPreconditionsFeatures(features);
		LOG.info("Feature-set changed from {} to {}", this.features, features);
		this.features = features;
	}
}
