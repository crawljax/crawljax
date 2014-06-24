package com.crawljax.core.state.duplicatedetection;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableCollection;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import ch.qos.logback.classic.Logger;

/**
 * Near-duplicate detection based on the use of a Jaccard coefficient. Given a set of features,
 * these features are first hashed. Afterwards the collections of hashes of SiteA and SiteB compared
 * using the Jaccard coefficients <code>(intersection(SiteA,SiteB)/union(SiteA,SiteB))</code>.
 */
@Singleton
public class NearDuplicateDetectionBroder implements NearDuplicateDetection {

	private static final Logger LOG = (Logger) LoggerFactory
	        .getLogger(NearDuplicateDetectionBroder.class);

	private ImmutableCollection<FeatureType> features;
	private double defaultThreshold;
	private HashGenerator hashGenerator;

	/**
	 * Default NearDuplicateDetectionBroder-constructor, the hashGenerator remains undefined until
	 * it is set using the setter.
	 * 
	 * @param threshold
	 *            the default threshold that should be provided to the fingerprints when generated.
	 * @param fs
	 *            the features that should be used to generate the fingerprints
	 */
	public NearDuplicateDetectionBroder(double threshold, ImmutableCollection<FeatureType> fs) {
		checkPreconditionsFeatures(fs);
		this.features = fs;
		this.defaultThreshold = threshold;
		LOG.info("NearDuplicateDetectionBroder[threshold=" + threshold + ", feature-list = " + fs
		        + "]");
	}

	/**
	 * The NearDuplicateDetectionBroder-constructor which also sets the HashGenerator.
	 * 
	 * @param threshold
	 *            the default threshold that should be provided to the fingerprints when generated.
	 * @param fs
	 *            the features that should be used to generate the fingerprints.
	 * @param hg
	 *            the hashGenerator used to generate the hashes inside the fingerprints.
	 */
	public NearDuplicateDetectionBroder(double threshold, ImmutableCollection<FeatureType> fs,
	        HashGenerator hg) {
		checkPreconditionsFeatures(fs);
		this.features = fs;
		this.defaultThreshold = threshold;
		this.hashGenerator = hg;
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
	private void checkPreconditionsFeatures(ImmutableCollection<FeatureType> features) {
		if (features == null || features.isEmpty()) {
			throw new DuplicateDetectionException(
			        "Invalid feature-list provided, feature-list cannot be null or empty. (Provided: "
			                + features + ")");
		}
	}

	public double getDefaultThreshold() {
		return defaultThreshold;
	}

	@Override
	public void setDefaultThreshold(double defaultThreshold) {
		LOG.info("Default threshold changed from {} to {}", this.defaultThreshold,
		        defaultThreshold);
		this.defaultThreshold = defaultThreshold;
	}

	@Override
	public ImmutableCollection<FeatureType> getFeatures() {
		return features;
	}

	@Override
	public void setFeatures(ImmutableCollection<FeatureType> features) {
		checkPreconditionsFeatures(features);
		LOG.info("Feature-set changed from {} to {}", this.features, features);
		this.features = features;
	}

	@Inject
	@Override
	public void setHashGenerator(HashGenerator hashGenerator) {
		this.hashGenerator = hashGenerator;
	}

	@Override
	public String toString() {
		return "NearDuplicateDetectionBroder [features=" + features + ", defaultThreshold="
		        + defaultThreshold + ", hashGenerator=" + hashGenerator + "]";
	}
}
