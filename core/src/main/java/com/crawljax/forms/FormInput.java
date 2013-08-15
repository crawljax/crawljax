/**
 * Created Aug 14, 2008
 */
package com.crawljax.forms;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification;

/**
 * @author mesbah
 */
public class FormInput {

	private long id;
	private String type = "text";

	private Identification identification;

	private Set<InputValue> inputValues = new HashSet<InputValue>();
	private Eventable eventable;

	private boolean multiple;

	public FormInput() {
		super();
	}

	/**
	 * @param type
	 *            the type of the input elements (e.g. text, checkbox)
	 * @param identification
	 *            the identification.
	 * @param value
	 *            the value of the elements. 1 for checked
	 */
	public FormInput(String type, Identification identification, String value) {
		this.type = type;
		this.identification = identification;
		inputValues.add(new InputValue(value, value.equals("1")));
	}

	/**
	 * @return the id.
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id.
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the input type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the input type.
	 */
	public void setType(String type) {
		if (!"".equals(type)) {
			this.type = type;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FormInput)) {
			return false;
		}

		if (this == obj) {
			return true;
		}
		final FormInput rhs = (FormInput) obj;

		return new EqualsBuilder().append(this.identification, rhs.getIdentification())
		        .append(this.type, rhs.getType()).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.identification).append(this.type).toHashCode();
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
	 *            form input set.
	 * @param identification
	 *            the identification to check.
	 * @return true if set contains a FormInput that has the same identification.
	 */
	public static boolean containsInput(Set<FormInput> inputs, Identification identification) {
		for (FormInput input : inputs) {
			if (input.getIdentification().equals(identification)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @param inputs
	 *            form input set.
	 * @param identification
	 *            the identification to check.
	 * @return a FormInput object that has the same identification.
	 */
	public static FormInput getInput(Set<FormInput> inputs, Identification identification) {
		for (FormInput input : inputs) {
			if (input.getIdentification().equals(identification)) {
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

	/**
	 * @return the identification
	 */
	public Identification getIdentification() {
		return identification;
	}

	/**
	 * @param identification
	 *            the identification to set
	 */
	public void setIdentification(Identification identification) {
		this.identification = identification;
	}
}
