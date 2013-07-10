package com.crawljax.core.state;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openqa.selenium.By;

/**
 * The Identification class, this class is used to denote a specific element. Specifying a method
 * and the value for that method.
 * 
 * @author mesbah
 */
public class Identification implements Serializable {
	private static final long serialVersionUID = -1608879189549535808L;

	/**
	 * The method used for identifying elements on the DOM tree.
	 */
	public enum How {
		xpath, name, id, tag, text, partialText
	}

	private long id;
	private How how;
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
	public Identification(How how, String value) {
		this.how = how;
		this.value = value;
	}

	/**
	 * @return the how
	 */
	public How getHow() {
		return how;
	}

	/**
	 * @param how
	 *            the how to set
	 */
	public void setHow(How how) {
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

		switch (how) {
			case name:
				return By.name(this.value);

			case xpath:
				// Work around HLWK driver bug
				return By.xpath(this.value.replaceAll("/BODY\\[1\\]/", "/BODY/"));

			case id:
				return By.id(this.value);

			case tag:
				return By.tagName(this.value);

			case text:
				return By.linkText(this.value);

			case partialText:
				return By.partialLinkText(this.value);

			default:
				return null;

		}

	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Identification)) {
			return false;
		}

		if (this == obj) {
			return true;
		}
		final Identification rhs = (Identification) obj;

		return new EqualsBuilder().append(this.how, rhs.getHow())
		        .append(this.value, rhs.getValue()).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.how).append(this.value).toHashCode();
	}
}
