package com.crawljax.oraclecomparator.comparators;

import com.crawljax.oraclecomparator.AbstractComparator;

/**
 * Simple oracle which can ignore whitespaces and linebreaks.
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
		String strippedStr;

		// remove linebreaks
		strippedStr = string.replaceAll("[\\t\\n\\x0B\\f\\r]", "");

		// remove just before and after elements spaces
		strippedStr = strippedStr.replaceAll(">[ ]*", ">");
		strippedStr = strippedStr.replaceAll("[ ]*<", "<");

		return strippedStr;
	}

}
