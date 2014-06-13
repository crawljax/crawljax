package com.crawljax.core.state.duplicatedetection;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Near-duplicate detection based on the use of a Jaccard coefficient. Given a set of features,
 * these features are first hashed. Afterwards the collections of hashes of SiteA and SiteB compared
 * using the Jaccard coefficients (intersection(SiteA,SiteB)/union(SiteA,SiteB)).
 */
@Singleton
public class NearDuplicateDetectionBroder implements NearDuplicateDetection {

	private static final Logger LOG = (Logger) LoggerFactory
	        .getLogger(NearDuplicateDetectionBroder.class);

	private List<FeatureType> features;
	private double defaultThreshold;
	private HashGenerator hashGenerator;
	private final static float THRESHOLD_UPPERLIMIT = 1;
	private final static float THRESHOLD_LOWERLIMIT = 0;

	@Inject
	public NearDuplicateDetectionBroder(double threshold, List<FeatureType> fs, HashGenerator hg) {
		checkPreconditionsFeatures(fs);
		checkPreconditionsThreshold(threshold);
		this.hashGenerator = hg;
		this.features = fs;
		this.defaultThreshold = threshold;
		LOG.info("NearDuplicateDetectionBroder[threshold=" + threshold + ", feature-list = " + fs
		        + ", HashGenerator= " + hg + "]");
	}

	/**
	 * Generate the hashes from the features of the string.
	 * 
	 * @param doc
	 *            The string that will be divided into features
	 * @return an array of the hashes, generated from the features, of the given string
	 */
	@Override
	public Fingerprint generateFingerprint(String doc) {
		// Check preconditions
		checkPreconditionsFeatures(features);
		checkPreconditionsThreshold(defaultThreshold);

		List<String> shingles = this.generateFeatures(doc);
		int length = shingles.size();

		int[] hashes = new int[length];
		for (int i = 0; i < length; i++) {
			hashes[i] = hashGenerator.generateHash(shingles.get(i));
		}
		return new BroderFingerprint(hashes, defaultThreshold);
	}

	/**
	 * Generate the features from the content of the state.
	 * 
	 * @param doc
	 *            The content of the state
	 * @return A list of strings that represent the features
	 * @throws FeatureException
	 *             if the feature size is to big or if the chosen feature type does not exist
	 */
	private List<String> generateFeatures(String doc) {
		List<String> li = new ArrayList<>();

		for (FeatureType feature : features) {
			li.addAll(feature.getFeatures(doc));
		}
		return li;
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
			        "Invalid feature-list provided, feature-list cannot be null or empty. (Provided: "
			                + features + ")");
		}
	}

	/**
	 * Checks the precondition for the threshold, which should be within the predefined upper and
	 * lower bounds.
	 * 
	 * @param threshold
	 */
	private void checkPreconditionsThreshold(double threshold) {
		if (threshold > THRESHOLD_UPPERLIMIT || threshold < THRESHOLD_LOWERLIMIT) {
			throw new DuplicateDetectionException("Invalid threshold value " + threshold
			        + ", threshold as to be between " + THRESHOLD_LOWERLIMIT + " and "
			        + THRESHOLD_UPPERLIMIT + ".");
		}
	}

	public double getDefaultThreshold() {
		return defaultThreshold;
	}

	public void setDefaultThreshold(double threshold) {
		checkPreconditionsThreshold(threshold);
		LOG.info("Threshold changed from {} to {}", this.defaultThreshold, threshold);
		this.defaultThreshold = threshold;
	}

	public List<FeatureType> getFeatures() {
		return features;
	}

	public void setFeatures(List<FeatureType> features) {
		checkPreconditionsFeatures(features);
		LOG.info("Feature-set changed from {} to {}", this.features, features);
		this.features = features;
	}
}
