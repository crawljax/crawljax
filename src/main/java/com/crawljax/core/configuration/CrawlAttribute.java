package com.crawljax.core.configuration;

/**
 * Specifies attribute for CrawlElements. Internal use only.
 * 
 * @author DannyRoest@gmail.com (Danny Roest)
 * @version $Id$
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
	protected CrawlAttribute(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public String toString() {
		return getName() + "=" + getValue();
	}

	/**
	 * @return the name
	 */
	protected String getName() {
		return name;
	}

	/**
	 * @return the value
	 */
	protected String getValue() {
		return value;
	}

}
