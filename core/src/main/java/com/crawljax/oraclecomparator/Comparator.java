package com.crawljax.oraclecomparator;

import java.util.List;

import net.jcip.annotations.NotThreadSafe;

import org.custommonkey.xmlunit.Difference;

/**
 * Interface for oracle comparators.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
@NotThreadSafe
public interface Comparator {

	/**
	 * @return The differences between the two DOMs
	 */
	List<Difference> getDifferences();

	/**
	 * @return if the originalDom and the newDom are equivalent
	 */
	boolean isEquivalent();

	/**
	 * @return The original DOM
	 */
	String getOriginalDom();

	/**
	 * @param originalDom
	 *            The original DOM.
	 */
	void setOriginalDom(String originalDom);

	/**
	 * @param newDom
	 *            The new DOM.
	 */
	void setNewDom(String newDom);

	/**
	 * @return The new DOM
	 */
	String getNewDom();
}
