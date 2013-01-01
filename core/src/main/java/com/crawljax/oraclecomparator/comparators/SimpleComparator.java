package com.crawljax.oraclecomparator.comparators;

import com.crawljax.oraclecomparator.AbstractComparator;

/**
 * Simple oracle which can ignore whitespaces and linebreaks.
 * 
 * @version $Id$
 */
public class SimpleComparator extends AbstractComparator {

	/**
	 * Default argument less constructor.
	 */
	public SimpleComparator() {
		super();
	}

	/**
	 * @param originalDom
	 *            The original DOM.
	 * @param newDom
	 *            The new DOM.
	 */
	public SimpleComparator(String originalDom, String newDom) {
		super(originalDom, newDom);
	}

	// removes whitespaces (before and after an element) and linebreaks
	@Override
	public boolean isEquivalent() {
		setOriginalDom(removeLinebreaksAndWhitespaces(getOriginalDom()));
		setNewDom(removeLinebreaksAndWhitespaces(getNewDom()));
		return super.compare();
	}

	private String removeLinebreaksAndWhitespaces(String string) {

		// remove linebreaks
		string = string.replaceAll("[\\t\\n\\x0B\\f\\r]", "");

		// remove just before and after elements spaces
		string = string.replaceAll(">[ ]*", ">");
		string = string.replaceAll("[ ]*<", "<");

		return string;
	}

}
