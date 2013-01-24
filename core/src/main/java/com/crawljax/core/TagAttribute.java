package com.crawljax.core;

import javax.annotation.concurrent.Immutable;

@Immutable
public class TagAttribute {

	private final String name;
	private final String value;

	/**
	 * @param name
	 *            name of the tag.
	 * @param value
	 *            value of the tag.
	 */
	public TagAttribute(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * @return the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the value.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Compare the value of this TagAttribute with a string. This string may contain wildcards by
	 * using a '%'. If the string contains this character, any occurrence of this character will be
	 * temporarily replaced with '.*' to compare using a regex, otherwise the strings will just be
	 * compared using equal().
	 * 
	 * @param str
	 *            an string.
	 * @author cor-paul
	 * @return whether the tag value matches the string.
	 */
	public boolean matchesValue(String str) {
		// check if we need to use wildcards
		if (this.value.contains("%")) {
			// replace with a useful wildcard for regex
			String pattern = this.value.replace("%", ".*");
			return str.matches(pattern);
		}
		// check if we are matching the class value; this may contain
		// multiple values!
		if (str.contains(" ")) {
			String[] classes = str.split(" ");
			for (int i = 0; i < classes.length; i++) {
				if (this.value.equals(classes[i])) {
					return true;
				}
			}
		}

		return this.value.equals(str);
	}
}
