package com.crawljax.oraclecomparator.comparators;

import com.crawljax.oraclecomparator.AbstractComparator;
import com.crawljax.util.EditDistance;

/**
 * Oracle Comparator that used the LevenStein Edit Distance to determince wheter two states are
 * equivalent.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $id$
 */
public class EditDistanceOracle extends AbstractComparator {

	private double treshold = 1;

	/**
	 * Default constructor with edit distance treshold = 1.
	 */
	public EditDistanceOracle() {

	}

	/**
	 * @param treshold
	 *            the edit distance treshold. 1 is no difference, 0 is totally different
	 */
	public EditDistanceOracle(double treshold) {
		this.treshold = treshold;
	}

	/**
	 * @return true if and only if the edit distance threshold is >= the specified treshold
	 */
	public boolean isEquivalent() {
		return EditDistance.isClone(getOriginalDom(), getNewDom(), getTreshold());
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

}
