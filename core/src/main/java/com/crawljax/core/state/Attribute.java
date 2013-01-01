package com.crawljax.core.state;

import java.io.Serializable;

/**
 * This class represents a attribute of a Element.
 * 
 * @see Element#Element(org.w3c.dom.Node)
 */
public class Attribute implements Serializable {

	private static final long serialVersionUID = -1608999189549539958L;

	private long id;
	private String name;
	private String value;

	/**
	 * Default constructor to support saving instances of this class as an XML.
	 */
	public Attribute() {

	}

	/**
	 * Create a new Attribute given a name and a value.
	 * 
	 * @param name
	 *            the name of the attribute
	 * @param value
	 *            the value of the attribute
	 */
	public Attribute(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}

	@Override
	public String toString() {
		return getName() + "=\"" + getValue() + "\"";
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
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
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
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

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
}
