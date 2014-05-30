package com.crawljax.core.state.duplicatedetection;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

@Singleton
public class NearDuplicateDetectionCrawlHash32 implements NearDuplicateDetection {

	private static final Logger LOG = LoggerFactory.getLogger(NearDuplicateDetectionCrawlHash32.class);
	
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
	
	private List<String> generateFeatures(String doc) throws FeatureException {
		List<String> li = new ArrayList<String>();
		for (FeatureType feature : features) {
			li.addAll(feature.getFeatures(doc));
		}
		return li;
	}

	@Override
	public int[] generateHash(String doc) throws FeatureException {
		assert doc != null;
		int bitLen = 32;
		int hash = 0x00000000;
		int one = 0x00000001; // 8
		int[] bits = new int[bitLen];
		List<String> tokens = this.generateFeatures(doc);
		for (String t : tokens) {
			int v = hashGenerator.generateHash(t);
			for (int i = bitLen; i >= 1; --i) {
				if (((v >> (bitLen - i)) & 1) == 1)
					++bits[i - 1];
				else
					--bits[i - 1];
			}
		}
		for (int i = bitLen; i >= 1; --i) {
			if (bits[i - 1] > 1) {
				hash |= one;
			}
			one = one << 1;
		}
		int[] hashArray = { hash };
		return hashArray;
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

	public HashGenerator getHashGenerator() {
		return hashGenerator;
	}

	@Override
	public void setThreshold(double threshold) {
		this.threshold = threshold;
		
	}

	public void setFeatures(List<FeatureType> features) {
		this.features = features;
	}
}
