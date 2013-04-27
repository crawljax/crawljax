package com.crawljax.oraclecomparator;

import java.util.List;

import net.jcip.annotations.NotThreadSafe;

import org.custommonkey.xmlunit.Difference;

import com.crawljax.util.DomUtils;

/**
 * The Abstract base class of all the Comparators. All comparators are not Thread safe as
 * comparators are shared between Threads and the origionalDom and newDom can not be final.
 */
@NotThreadSafe
public abstract class AbstractComparator implements Comparator {

	@Override
	public List<Difference> getDifferences(String oldDom, String newDom) {
		return DomUtils.getDifferences(normalize(oldDom), normalize(newDom));
	}

	@Override
	public boolean isEquivalent(String oldDom, String newDom) {
		boolean equivalent = false;
		if (StateComparator.COMPARE_IGNORE_CASE) {
			equivalent = normalize(oldDom).equalsIgnoreCase(normalize(newDom));
		} else {
			equivalent = normalize(oldDom).equals(normalize(newDom));
		}
		return equivalent;
	}

	/**
	 * Override this method to apply normalization to the comparison.
	 * 
	 * @param dom
	 *            The original DOM
	 * @return the normalized DOM.
	 */
	@Override
	public String normalize(String dom) {
		return dom;
	}
}
