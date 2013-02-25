package com.crawljax.core.configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a form input field NOTE: In general InputField is not designed to be instantiated
 * directly. For example: <input type="text" name="foo" /> <input type="checkbox" id="bar" />
 * FormInputSpecification input = new FormInputSpecification()
 * input.field("foo").setValue("Crawljax"); input.field("bar").setValue(true); Crawljax will set the
 * text value of the foo text field to "Crawljax" and checks the checkbox with id bar.
 * 
 * @see InputSpecification#field(String)
 * @see InputSpecification#fields(String...)
 * @author DannyRoest@gmail.com (Danny Roest)
 */
public class InputField {

	private final String id;
	private final List<String> fieldNames = new ArrayList<String>();
	private final List<String> fieldValues = new ArrayList<String>();

	/**
	 * Constructor.
	 */
	protected InputField() {
		this.id = "id" + hashCode();
	}

	/**
	 * Sets the value of this input field with a text input. Applicable to all form elements except
	 * checkboxes and a radio buttons.
	 * 
	 * @param value
	 *            Value to set.
	 * @return this InputField
	 */
	public InputField setValue(String value) {
		this.fieldValues.add(value);
		return this;
	}

	/**
	 * Sets the value of this input field. Only Applicable checkboxes and a radio buttons.
	 * 
	 * @param value
	 *            Value to set.
	 * @return This object.
	 */
	public InputField setValue(boolean value) {
		if (value) {
			this.fieldValues.add("1");
		} else {
			this.fieldValues.add("0");
		}
		return this;
	}

	@Override
	public String toString() {
		return fieldNames.toString() + "=" + fieldValues.toString();
	}

	// not visible

	/**
	 * @param fieldName
	 *            Field name to set.
	 */
	protected void setFieldName(String fieldName) {
		this.fieldNames.add(fieldName);
	}

	/**
	 * @param fieldNames
	 *            Field names to set.
	 */
	protected void setFieldNames(String... fieldNames) {
		for (String fieldName : fieldNames) {
			this.fieldNames.add(fieldName);
		}
	}

	/**
	 * @return the id
	 */
	protected String getId() {
		return id;
	}

	/**
	 * @return the fieldNames
	 */
	protected List<String> getFieldNames() {
		return fieldNames;
	}

	/**
	 * @return The value.
	 */
	protected String getValue() {
		if (fieldValues.isEmpty()) {
			return null;
		} else {
			return fieldValues.get(0);
		}
	}

	/**
	 * @return the fieldValues
	 */
	protected List<String> getFieldValues() {
		return fieldValues;
	}
}
