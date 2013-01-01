package com.crawljax.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Edit Distance class.
 * 
 * @author mesbah
 * @version $Id$
 */
public final class EditDistance {
	private static final Logger LOGGER = LoggerFactory.getLogger(EditDistance.class.getName());

	private EditDistance() {
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
	public static double getThreshold(String x, String y, double p) {
		return 2 * Math.max(x.length(), y.length()) * (1 - p);
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
	public static boolean isClone(String str1, String str2, double thresholdCoef) {
		LOGGER.info("Calculating the Edit Distance with threshold-coef: " + thresholdCoef);

		if ((thresholdCoef < 0.0) || (thresholdCoef > 1.0)) {
			throw new IllegalArgumentException(
			        "Threshold Coefficient must be between 0.0 and 1.0!");
		}

		if (StringUtils.getLevenshteinDistance(str1, str2) <= getThreshold(str1, str2,
		        thresholdCoef)) {

			return true;
		}

		return false;
	}
}
