package com.crawljax.core.state;

import java.io.Serializable;

import org.openqa.selenium.By;

/**
 * The Identification class, this class is used to denote a specific element. Specifying a method
 * and the value for that method.
 * 
 * @author mesbah
 * @version $Id$
 */
public class Identification implements Serializable, Cloneable {
	private static final long serialVersionUID = -1608879189549535808L;
	private long id;
	private String how;
	private String value;

	/**
	 * Default constructor to support saving instances of this class as an XML.
	 */
	public Identification() {

	}

	/**
	 * Create a new Identification.
	 * 
	 * @param how
	 *            the method of identification (xpath, id, name, ...)
	 * @param value
	 *            the value of the identification method.
	 */
	public Identification(String how, String value) {
		this.how = how;
		this.value = value;
	}

	/**
	 * @return the how
	 */
	public String getHow() {
		return how;
	}

	/**
	 * @param how
	 *            the how to set
	 */
	public void setHow(String how) {
		this.how = how;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Convert a Identification to a String.
	 * 
	 * @return the String representation of the Identification
	 */
	@Override
	public String toString() {
		return this.how + " " + this.value;
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
	 * Convert a Identification to a By used in WebDriver Drivers.
	 * 
	 * @return the correct By specification of the current Identification.
	 */
	public By getWebDriverBy() {
		if (how.equals("name")) {
			return By.name(this.value);
		}

		if (how.equals("xpath")) {
			return By.xpath(this.value);
		}

		if (how.equals("id")) {
			return By.id(this.value);
		}

		if (how.equals("tag")) {
			return By.tagName(this.value);
		}

		return null;
	}

	/**
	 * Get a clone of this object. {@inheritDoc}
	 */
	@Override
	public Identification clone() {
		Identification id = new Identification();
		id.setHow(this.how);
		id.setId(this.id);
		id.setValue(this.value);
		return id;
	}
}
