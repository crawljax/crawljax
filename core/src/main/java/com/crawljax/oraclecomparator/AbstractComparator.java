package com.crawljax.oraclecomparator;

import java.util.List;

import net.jcip.annotations.NotThreadSafe;

import org.custommonkey.xmlunit.Difference;

import com.crawljax.util.Helper;

/**
 * The Abstract base class of all the Comparators. All comparators are not Thread safe as
 * comparators are shared between Threads and the origionalDom and newDom can not be final.
 * 
 * @author Danny
 * @version $Id$
 */
@NotThreadSafe
public abstract class AbstractComparator implements Comparator {

	private String originalDom;
	private String newDom;

	/**
	 * Constructor.
	 */
	public AbstractComparator() {

	}

	/**
	 * @param originalDom
	 *            The original DOM.
	 * @param newDom
	 *            The new DOM.
	 */
	public AbstractComparator(String originalDom, String newDom) {
		this.originalDom = originalDom;
		this.newDom = newDom;
	}

	/**
	 * @return If the original DOM and the new DOM are equal. Note: Via
	 *         OracleControllerConfiguration ignore case can be set
	 */
	protected boolean compare() {
		boolean equivalent = false;
		if (StateComparator.COMPARE_IGNORE_CASE) {
			equivalent = getOriginalDom().equalsIgnoreCase(getNewDom());
		} else {
			equivalent = getOriginalDom().equals(getNewDom());
		}
		return equivalent;
	}

	/**
	 * @return Whether they are equivalent.
	 */
	@Override
	public abstract boolean isEquivalent();

	/**
	 * @return Differences between the DOMs.
	 */
	@Override
	public List<Difference> getDifferences() {
		return Helper.getDifferences(getOriginalDom(), getNewDom());
	}

	/**
	 * @return The original DOM.
	 */
	@Override
	public String getOriginalDom() {
		return originalDom;
	}

	/**
	 * @param originalDom
	 *            The new original DOM.
	 */
	@Override
	public void setOriginalDom(String originalDom) {
		this.originalDom = originalDom;
	}

	/**
	 * @return The new DOM.
	 */
	@Override
	public String getNewDom() {
		return newDom;
	}

	/**
	 * @param newDom
	 *            The new DOM.
	 */
	@Override
	public void setNewDom(String newDom) {
		this.newDom = newDom;
	}

}
