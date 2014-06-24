package com.crawljax.core.state.duplicatedetection;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableCollection;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import ch.qos.logback.classic.Logger;

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

	private ImmutableCollection<FeatureType> features;
	private double defaultThreshold;
	private HashGenerator hashGenerator;
	private static final Logger LOG = (Logger) LoggerFactory
	        .getLogger(NearDuplicateDetectionCrawlhash.class);

	/**
	 * Default constructor, the hashGenerator remains undefined until it is set using the setter.
	 * 
	 * @param threshold
	 *            the default threshold that should be provided to the fingerprints when generated.
	 * @param fs
	 *            the features that should be used to generate the fingerprints
	 */
	public NearDuplicateDetectionCrawlhash(double threshold, ImmutableCollection<FeatureType> fs) {
		checkPreconditionsFeatures(fs);
		this.features = fs;
		this.defaultThreshold = threshold;
	}

	/**
	 * The constructor which also sets the HashGenerator.
	 * 
	 * @param threshold
	 *            the default threshold that should be provided to the fingerprints when generated.
	 * @param fs
	 *            the features that should be used to generate the fingerprints.
	 * @param hg
	 *            the hashGenerator used to generate the hashes inside the fingerprints.
	 */
	public NearDuplicateDetectionCrawlhash(double threshold, ImmutableCollection<FeatureType> fs,
	        HashGenerator hg) {
		checkPreconditionsFeatures(fs);
		this.features = fs;
		this.defaultThreshold = threshold;
		this.hashGenerator = hg;
	}

	@Override
	public Fingerprint generateFingerprint(String doc) {
		checkPreconditionsFeatures(features);
		int[] bits = new int[HASH_LENGTH];
		List<String> tokens = this.generateFeatures(doc);
		// loop through all tokens (ie shingles), calculate the hash, and add
		// the hash to the array.
		for (String token : tokens) {
			int hash = hashGenerator.generateHash(token);
			bits = addHashToArray(hash, bits);
		}
		return new CrawlhashFingerprint(projectArrayOnHash(bits), defaultThreshold);
	}

	/**
	 * ' Generates a list of features provided some string.
	 * 
	 * @param doc
	 *            the document for which features should be generated. The document should be larger
	 *            than the feature-size.
	 * @return a list of generated features of the document.
	 */
	private List<String> generateFeatures(String doc) {
		List<String> li = new ArrayList<>();
		for (FeatureType feature : features) {
			li.addAll(feature.getFeatures(doc));
		}
		return li;
	}

	/**
	 * Creates a bit representation of the bits array. All positive integers will be transformed to
	 * a 1 and all negative integers are transformed to a 0. So {8,9,-2,0} -> 1100
	 * 
	 * @param bits
	 *            the original array of integers
	 * @return integer representing the bits.
	 */
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

	/**
	 * Adds a hash to a compounded array of bits
	 * 
	 * @param hash
	 *            the hash to be added to the compounded array
	 * @param bits
	 *            the compounded array
	 * @return bits with positions, where hash has 1's, incremented by one
	 */
	private int[] addHashToArray(int hash, int[] bits) {
		// Loop through each bit-position, starting with the least significant.
		for (int i = HASH_LENGTH; i >= 1; --i) {
			// check if bit-position is one, if so add 1 to array at current position.
			if (((hash >> (HASH_LENGTH - i)) & 1) == 1) {
				++bits[i - 1];
			} else {
				--bits[i - 1];
			}
		}
		return bits;
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
			        "Invalid feature-list provided, feature-list cannot be "
			                + "null or empty. (Provided: " + features + ")");
		}
	}

	@Override
	public double getDefaultThreshold() {
		return defaultThreshold;
	}

	@Override
	public ImmutableCollection<FeatureType> getFeatures() {
		return features;
	}

	@Override
	public void setDefaultThreshold(double defaultThreshold) {
		LOG.info("Default threshold changed from {} to {}", this.defaultThreshold,
		        defaultThreshold);
		this.defaultThreshold = defaultThreshold;
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
		return "NearDuplicateDetectionCrawlhash [features=" + features + ", defaultThreshold="
		        + defaultThreshold + ", hashGenerator=" + hashGenerator + "]";
	}
}
