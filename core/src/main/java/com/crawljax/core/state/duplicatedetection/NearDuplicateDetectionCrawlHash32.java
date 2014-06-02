package com.crawljax.core.state.duplicatedetection;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class NearDuplicateDetectionCrawlHash32 implements NearDuplicateDetection {

	private static final Logger LOG = LoggerFactory.getLogger(NearDuplicateDetectionCrawlHash32.class);
	private static final int HASH_LENGTH = 32;
	private static final int HEX_ONE = 0x00000001;
	private static final int HEX_ZERO = 0x00000000;
	
	private List<FeatureType> features;
	private double threshold;
	private HashGenerator hashGenerator;
	
	@Inject
	public NearDuplicateDetectionCrawlHash32(HashGenerator hg) {
		this.hashGenerator = hg;
		fillFeatures(null);
		this.threshold = 0;
	}
	
	public NearDuplicateDetectionCrawlHash32(double threshold, List<FeatureType> fs, HashGenerator hg) {
		this.hashGenerator = hg;
		fillFeatures(fs);
		this.threshold = threshold;
	}
	
	private void fillFeatures(List<FeatureType> fs) {
		this.features = fs != null ? fs : new ArrayList<FeatureType>();
	}
	
	private List<String> generateFeatures(String doc) {
		List<String> li = new ArrayList<String>();
		for(FeatureType feature : features) {
			li.addAll(feature.getFeatures(doc));
		}
		return li;
	}
	
	@Override
	public int[] generateHash(String doc) {
		assert doc != null;
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
		LOG.info("Comparing hash {} with hash {} using a threshold of {}", hash1[0], hash2[0], threshold);
		return ((double) hammingDistance(hash1[0],hash2[0])) <= threshold;
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
		this.threshold = threshold;
		
	}

	public void setFeatures(List<FeatureType> features) {
		this.features = features;
	}
}
