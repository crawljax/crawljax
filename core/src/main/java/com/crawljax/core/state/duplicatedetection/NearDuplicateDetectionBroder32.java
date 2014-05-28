package com.crawljax.core.state.duplicatedetection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * Near-duplicate detection based on the Jaccard coefficient and shingles.
 */
public class NearDuplicateDetectionBroder32 implements NearDuplicateDetection {

	private List<FeatureType> features;
	private double treshold;
	private HashGenerator hashGenerator;
	
	public NearDuplicateDetectionBroder32(double t, List<FeatureType> fs, HashGenerator hg) {
		this.treshold = t;
		this.features = fs;
		this.hashGenerator = hg;
	}
	
	/**
	 * Generate the hashes from the features of the string.
	 * @param doc The string that will be divided into features
	 * @return an array of the hashes, generated from the features, of the given string
	 */
	@Override
	public int[] generateHash(String doc) throws FeatureException {
		List<String> shingles = this.generateFeatures(doc);
		int length = shingles.size();
		
		int[] hashes = new int[length];
		for (int i=0; i<length; i++) {
			hashes[i] = hashGenerator.generateHash(shingles.get(i));
		}
		return hashes;
	}

	/**
	 * Return true if the JaccardCoefficient is higher than the treshold.
	 */
	@Override
	public boolean isNearDuplicateHash(int[] state1, int[] state2) {
		return (this.getDistance(state1, state2) >= this.treshold);
	}
	
	/**
	 * Get the distance between two sets.
	 * @return One if both sets are completely different and zero if the two sets are exactly the same.
	 */
	@Override
	public double getDistance(int[] state1, int[] state2) {
		//double jaccardCoefficient = this.getJaccardCoefficient(state1, state2);
		//return 1 - jaccardCoefficient;
		return this.getJaccardCoefficient(state1, state2);
	}
	
	private double getJaccardCoefficient(int[] state1, int[] state2) {
		HashSet<Integer> setOfFirstArg = new HashSet<Integer>();
		HashSet<Integer> setOfSecondArg = new HashSet<Integer>();
		for (int i=0; i<state1.length; i++) {
			setOfFirstArg.add(state1[i]);
		}
		for (int j=0; j<state2.length; j++) {
			setOfSecondArg.add(state2[j]);
		}
		
		double unionCount = this.unionCount(setOfFirstArg, setOfSecondArg);
		double intersectionCount = this.intersectionCount(setOfFirstArg, setOfSecondArg);
		
		return (intersectionCount / unionCount);
	}
	
	/**
	 * Generate the features from the content of the state.
	 * @param doc The content of the state
	 * @return A list of strings that represent the features
	 * @throws FeatureException if the feature sie is to big of if the chosen feature type does not exist
	 */
	private List<String> generateFeatures(String doc) throws FeatureException {
		List<String> li = new ArrayList<String>();
			
		for(FeatureType feature : features) {
			li.addAll(feature.getFeatures(doc));
		}
		return li;
	}
	
	private int unionCount(Set<Integer> list1, Set<Integer> list2) {
		Set<Integer> union = Sets.union(list1, list2);
		return union.size();
	}
	
	private int intersectionCount(Set<Integer> list1, Set<Integer> list2) {
		Set<Integer> intersection = Sets.intersection(list1, list2);
		return intersection.size();
	}
}
