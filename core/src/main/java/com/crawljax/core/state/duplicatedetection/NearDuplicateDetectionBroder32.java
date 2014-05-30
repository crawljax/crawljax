package com.crawljax.core.state.duplicatedetection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

import com.google.common.collect.Sets;
import com.google.inject.Inject;

/**
 * Near-duplicate detection based on the Jaccard coefficient and shingles.
 */
@Singleton
public class NearDuplicateDetectionBroder32 implements NearDuplicateDetection {

	private List<FeatureType> features;
	private double treshold;
	private HashGenerator hashGenerator;

	public NearDuplicateDetectionBroder32(double t, List<FeatureType> fs, HashGenerator hg) {
		this.treshold = t;
		this.hashGenerator = hg;
		fillFeatures(fs);
	}
	
	@Inject
	public NearDuplicateDetectionBroder32(HashGenerator hg) {
		this.treshold = 3; //TODO Guice annotated?
		this.hashGenerator = hg;
		fillFeatures(null);
	}
	
	private void fillFeatures(List<FeatureType> fs) {
		this.features = fs != null ? fs : new ArrayList<FeatureType>();
	}

	public double getThreshold() {
		return treshold;
	}

	/**
	 * Generate the hashes from the features of the string.
	 * 
	 * @param doc
	 *            The string that will be divided into features
	 * @return an array of the hashes, generated from the features, of the given string
	 */
	@Override
	public int[] generateHash(String doc) throws FeatureException {
		List<String> shingles = this.generateFeatures(doc);
		int length = shingles.size();

		int[] hashes = new int[length];
		for (int i = 0; i < length; i++) {
			hashes[i] = hashGenerator.generateHash(shingles.get(i));
		}
		return hashes;
	}

	/**
	 * Return true if the JaccardCoefficient is higher than the threshold.
	 */
	@Override
	public boolean isNearDuplicateHash(int[] state1, int[] state2) {
		return (this.getDistance(state1, state2) <= this.treshold);
	}

	/**
	 * Get the distance between two sets.
	 * 
	 * @return Zero if both sets contains exactly the same hashes and one if the two sets contains
	 *         all different hashes and values in between for the corresponding difference. The
	 *         closer the value is to zero, the more hashes in the sets are the same.
	 */
	@Override
	public double getDistance(int[] state1, int[] state2) {
		double jaccardCoefficient = this.getJaccardCoefficient(state1, state2);
		return 1 - jaccardCoefficient;
	}

	private double getJaccardCoefficient(int[] state1, int[] state2) {
		Set<Integer> setOfFirstArg = new HashSet<Integer>(state1.length);
		Set<Integer> setOfSecondArg = new HashSet<Integer>(state2.length);
		for (int state : state1) {
			setOfFirstArg.add(state);
		}
		for (int state : state2) {
			setOfSecondArg.add(state);
		}

		double unionCount = this.unionCount(setOfFirstArg, setOfSecondArg);
		double intersectionCount = this.intersectionCount(setOfFirstArg, setOfSecondArg);

		return (intersectionCount / unionCount);
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
	private List<String> generateFeatures(String doc) throws FeatureException {
		List<String> li = new ArrayList<>();

		for (FeatureType feature : features) {
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

	public double getTreshold() {
		return treshold;
	}

	public void setThreshold(double threshold) {
		this.treshold = threshold;
	}

	public List<FeatureType> getFeatures() {
		return features;
	}

	public HashGenerator getHashGenerator() {
		return hashGenerator;
	}
	
	public void setFeatures(List<FeatureType> features) {
		this.features = features;	
	}	
}
