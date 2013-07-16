package com.crawljax.oraclecomparator.comparators;

import org.apache.commons.lang3.StringUtils;

import com.crawljax.oraclecomparator.AbstractComparator;

/**
 * Oracle Comparator that uses the Levenshtein Edit Distance to determince wheter two states are
 * equivalent.
 */
public class EditDistanceComparator extends AbstractComparator {

	private double treshold = 1;

	/**
	 * Default constructor with edit distance treshold = 1.
	 */
	public EditDistanceComparator() {

	}

	/**
	 * @param treshold
	 *            the edit distance treshold. 1 is no difference, 0 is totally different
	 */
	public EditDistanceComparator(double treshold) {
		this.treshold = treshold;
	}

	/**
	 * @return true if and only if the edit distance threshold is >= the specified treshold
	 */
	@Override
	public boolean isEquivalent(String oldDom, String newDom) {
		return isClone(oldDom, newDom, getTreshold());
	}

	/**
	 * @return the treshold
	 */
	public double getTreshold() {
		return treshold;
	}

	/**
	 * @param treshold
	 *            the treshold to set
	 */
	public void setTreshold(double treshold) {
		this.treshold = treshold;
	}

	/**
	 * @param str1
	 *            the first string.
	 * @param str2
	 *            the second string.
	 * @param thresholdCoef
	 *            the threshold coefficient: must be between 0.0-1.0.
	 * @return true if the Levenshtein distance is lower than or equal to the computed threshold.
	 */
	boolean isClone(String str1, String str2, double thresholdCoef) {
		if ((thresholdCoef < 0.0) || (thresholdCoef > 1.0)) {
			throw new IllegalArgumentException(
			        "Threshold Coefficient must be between 0.0 and 1.0!");
		} else if (StringUtils.getLevenshteinDistance(str1, str2) <= getThreshold(str1, str2,
		        thresholdCoef)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Calculate a threshold.
	 * 
	 * @param x
	 *            first string.
	 * @param y
	 *            second string.
	 * @param p
	 *            the threshold coefficient.
	 * @return 2 maxLength(x, y) (1-p)
	 */
	double getThreshold(String x, String y, double p) {
		return 2 * Math.max(x.length(), y.length()) * (1 - p);
	}
}
