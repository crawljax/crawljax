package com.crawljax.core.state.duplicatedetection;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jpountz.xxhash.XXHash32;
import net.jpountz.xxhash.XXHashFactory;

public class NearDuplicateDetectionCrawlHash32 implements NearDuplicateDetection {

	private static final Logger logger = LoggerFactory.getLogger(NearDuplicateDetectionCrawlHash32.class);
	
	private XXHash32 xxhash;
	private List<FeatureType> features;
	private int threshold;
	
	public NearDuplicateDetectionCrawlHash32(int threshold, List<FeatureType> fs) {
		xxhash = XXHashFactory.fastestInstance().hash32();
		features = fs;
		this.threshold = threshold;
	}
	
	private List<String> generateFeatures(String doc) throws FeatureShinglesException {
		List<String> li = new ArrayList<String>();
			
		for(FeatureType feature : features) {
			li.addAll(feature.getFeatures(doc));
		}
		return li;
	}
	
	@Override
	public int generateHash(String doc) throws FeatureShinglesException{
		assert doc != null;
		int bitLen = 32;
		int hash = 0x00000000;
		int one = 0x00000001; //8
		int[] bits = new int[bitLen];
		List<String> tokens = this.generateFeatures(doc);
		for (String t : tokens) {
			int v = xxhash.hash(t.getBytes(), 0, t.length(), 0x9747b28c);
			logger.debug(String.valueOf(v));
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
		logger.debug("hash: " + String.valueOf(hash));
		return hash;
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
	public boolean hasNearDuplicateHash(int hash) {
		// Not supported in this implementation
		logger.warn("hasNearDuplicateHash/1 is not supported by NDD-crawlhash32");
		return false;
	}

	@Override
	public int findNearDuplicateHash(int hash) {
		// Not supported in this implementation
		logger.warn("findNearDuplicateHash/1 is not supported by NDD-crawlhash32");
		return 0;
	}

	@Override
	public boolean isNearDuplicateHash(int hash1, int hash2) {
		return hammingDistance(hash1,hash2) <= threshold;
	}	
	
	public int getThreshold() {
		return threshold;
	}

	@Override
	public int getDistance(int hash1, int hash2) {
		return hammingDistance(hash1, hash2);
	}
}
