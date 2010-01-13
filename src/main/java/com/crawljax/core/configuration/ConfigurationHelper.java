package com.crawljax.core.configuration;

import java.util.List;

/**
 * Helper class for configurations.
 * 
 * @author Danny Roest (dannyroest@gmail.com)
 * @version $Id$
 */
public final class ConfigurationHelper {

	private ConfigurationHelper() {
	}

	/**
	 * @param items The items to be added to the string.
	 * @return string representation of list. format: a, b, , c. Empty String allowed
	 */
	public static String listToStringEmptyStringAllowed(List<String> items) {
		String str = "";
		int i = 0;
		for (String item : items) {
			if (i > 0) {
				str += ", ";
			}
			str += item;
			i++;
		}
		return str;
	}

	/**
	 * @param items The items to be added to the string.
	 * @return string representation of list. format: a, b, c
	 */
	public static String listToString(List<?> items) {
		String str = "";
		for (Object item : items) {
			if (!str.equals("")) {
				str += ", ";
			}
			str += item.toString();
		}
		return str;
	}

	/**
	 * @param value The value to be converted
	 * @return int value of boolean, true=1 false=0
	 */
	public static int booleanToInt(boolean value) {
		if (value) {
			return 1;
		} else {
			return 0;
		}
	}

}
