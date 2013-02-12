package com.crawljax.core.state;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;

/**
 * This class represents a attribute of a Element.
 * 
 * @see Element#Element(org.w3c.dom.Node)
 */
@Immutable
public class Attribute implements Serializable {

	private static final long serialVersionUID = -1608999189549539958L;

	private final String name;
	private final String value;

	/**
	 * Create a new Attribute given a name and a value.
	 * 
	 * @param name
	 *            the name of the attribute
	 * @param value
	 *            the value of the attribute
	 */
	public Attribute(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public String toString() {
		return getName() + "=\"" + getValue() + "\"";
	}

	/**
	 * @return the name
	 */
	public String getName() {
		if (name != null) {
			return name.trim();
		}

		return name;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		if (value != null) {
			return value.trim();

		}
		return value;
	}

}
