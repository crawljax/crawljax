package com.crawljax.oraclecomparator;

import net.jcip.annotations.NotThreadSafe;
import org.custommonkey.xmlunit.Difference;

import java.util.List;

/**
 * Interface for oracle comparators.
 */
@NotThreadSafe
public interface Comparator {

	/**
	 * @return The differences between the two DOMs
	 */
	List<Difference> getDifferences(String oldDom, String newDom);

	/**
	 * @return if the originalDom and the newDom are equivalent
	 */
	boolean isEquivalent(String oldDom, String newDom);

	/**
	 * @return The normalized DOM, on which the comparison is made.
	 */
	String normalize(String dom);

}
