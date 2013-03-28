package com.crawljax.core.configuration;

import com.google.common.base.Objects;

/**
 * Specifies attribute for CrawlElements. Internal use only.
 * 
 * @author DannyRoest@gmail.com (Danny Roest)
 */
public class CrawlAttribute {

	private final String name;
	private final String value;

	/**
	 * Note use a % as wildcard in name of value.
	 * 
	 * @param name
	 *            Name.
	 * @param value
	 *            Value.
	 */
	public CrawlAttribute(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Compare the value of this CrawlAttribute with a string. This string may contain wildcards by
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

	@Override
	public int hashCode() {
		return Objects.hashCode(name, value);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof CrawlAttribute) {
			CrawlAttribute that = (CrawlAttribute) object;
			return Objects.equal(this.name, that.name)
			        && Objects.equal(this.value, that.value);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
		        .add("name", name)
		        .add("value", value)
		        .toString();
	}

}
