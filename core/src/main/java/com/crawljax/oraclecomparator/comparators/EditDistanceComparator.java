package com.crawljax.oraclecomparator.comparators;

import com.crawljax.oraclecomparator.AbstractComparator;
import org.apache.commons.text.similarity.LevenshteinDistance;

/**
 * Oracle Comparator that uses the Levenshtein Edit Distance to determine whether two states are
 * equivalent.
 */
public class EditDistanceComparator extends AbstractComparator {

	private double threshold = 1;

	/**
	 * Default constructor with edit distance threshold = 1.
	 */
	public EditDistanceComparator() {

	}

	/**
	 * @param threshold the edit distance threshold. 1 is no difference, 0 is totally different
	 */
	public EditDistanceComparator(double threshold) {
		this.threshold = threshold;
	}

	/**
	 * @return true if and only if the edit distance threshold is &gt;= the specified threshold
	 */
	@Override
	public boolean isEquivalent(String oldDom, String newDom) {
		return isClone(oldDom, newDom, getThreshold());
	}

	/**
	 * @return the threshold
	 */
	public double getThreshold() {
		return threshold;
	}

	/**
	 * @param threshold the threshold to set
	 */
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	/**
	 * @param str1          		the first string.
	 * @param str2          		the second string.
	 * @param thresholdCoefficient 	the threshold coefficient: must be between 0.0-1.0.
	 * @return 						true if the Levenshtein distance is lower than or equal to the computed threshold.
	 */
	boolean isClone(String str1, String str2, double thresholdCoefficient) {
		if ((thresholdCoefficient < 0.0) || (thresholdCoefficient > 1.0)) {
			throw new IllegalArgumentException(
					"Threshold Coefficient must be between 0.0 and 1.0!");
		} else
			return LevenshteinDistance.getDefaultInstance().apply(str1, str2) <= getThreshold(
					str1, str2, thresholdCoefficient);
	}

	/**
	 * Calculate a threshold.
	 *
	 * @param x first string.
	 * @param y second string.
	 * @param p the threshold coefficient.
	 * @return 2 maxLength(x, y) (1-p)
	 */
	double getThreshold(String x, String y, double p) {
		return 2 * Math.max(x.length(), y.length()) * (1 - p);
	}
}
