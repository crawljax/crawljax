/**
 * Created Aug 14, 2008
 */
package com.crawljax.forms;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.crawljax.core.state.Eventable;

/**
 * @author mesbah
 * @version $Id: FormInput.java 6359 2009-12-28 14:37:49Z danny $
 */
public class FormInput {

	private long id;
	private String type = "text";
	private String name;
	private Set<InputValue> inputValues = new HashSet<InputValue>();
	private Eventable eventable;
	// public int index;

	private boolean multiple;

	/**
	 *
	 */
	public FormInput() {
		super();
	}

	/**
	 * @param type
	 *            the type of the input elements (e.g. text, checkbox)
	 * @param name
	 *            the id or name of the elements
	 * @param value
	 *            the value of the elements. 1 for checked
	 */
	public FormInput(String type, String name, String value) {
		this.type = type;
		this.name = name;
		inputValues.add(new InputValue(value, value.equals("1")));
	}

	/**
	 * @return TODO: DOCUMENT ME!
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id
	 *            TODO: DOCUMENT ME!
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return TODO: DOCUMENT ME!
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            TODO: DOCUMENT ME!
	 */
	public void setType(String type) {
		if (!"".equals(type)) {
			this.type = type;
		}
	}

	/**
	 * @return DOCUMENT ME!
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            DOCUMENT ME!
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || getType() == null || getName() == null) {
			return false;
		}
		FormInput formInput = (FormInput) obj;
		return getName().equals(formInput.getName()) && getType().equals(formInput.getType());
	}

	@Override
	public int hashCode() {
		return getName().hashCode() + getType().hashCode();
	}

	/**
	 * @return whether the element can have multiple values
	 */
	public boolean isMultiple() {
		return multiple;
	}

	/**
	 * @param multiple
	 *            set whether the elements ca have multiple values
	 */
	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

	/**
	 * @param inputs
	 *            DOCUMENT ME!
	 * @param name
	 *            DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static boolean containsInput(Set<FormInput> inputs, String name) {
		for (FormInput input : inputs) {
			if (input.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @param inputs
	 *            DOCUMENT ME!
	 * @param name
	 *            DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static FormInput getInput(Set<FormInput> inputs, String name) {
		for (FormInput input : inputs) {
			if (input.getName().equalsIgnoreCase(name)) {
				return input;
			}
		}

		return null;
	}

	/**
	 * @return the inputValues
	 */
	public Set<InputValue> getInputValues() {
		return inputValues;
	}

	/**
	 * @param inputValues
	 *            the inputValues to set
	 */
	public void setInputValues(Set<InputValue> inputValues) {
		this.inputValues = inputValues;
	}

	/**
	 * @return the related eventable for submitting
	 */
	public Eventable getEventable() {
		return eventable;
	}

	/**
	 * @param eventable
	 *            the eventable by which this FormInput is submitted by
	 */
	public void setEventable(Eventable eventable) {
		this.eventable = eventable;
	}

}
