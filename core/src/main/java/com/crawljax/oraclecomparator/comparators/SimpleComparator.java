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

	@Override
	public String normalize(String string) {
		String strippedStr;

		// remove linebreaks
		strippedStr = string.replaceAll("[\\t\\n\\x0B\\f\\r]", "");

		// remove just before and after elements spaces
		strippedStr = strippedStr.replaceAll(">[ ]*", ">");
		strippedStr = strippedStr.replaceAll("[ ]*<", "<");

		return strippedStr;
	}

}
