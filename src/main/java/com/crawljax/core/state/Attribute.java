package com.crawljax.core.state;

/**
 * This class represents a attribute of a Element.
 * 
 * @see {@link Element#Element(org.w3c.dom.Node)}
 * @author mesbah
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id$
 */
public class Attribute implements Cloneable {

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
		return name.trim();
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
		return value.trim();
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Get a clone of this object. {@inheritDoc}
	 */
	@Override
	public Attribute clone() {
		Attribute a = new Attribute();
		a.setId(this.id);
		a.setName(this.name);
		a.setValue(this.value);
		return a;
	}
}
