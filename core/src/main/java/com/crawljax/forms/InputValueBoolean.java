package com.crawljax.forms;

/**
 * Value for a FormInputs that take a boolean type input.
 */
public class InputValueBoolean extends InputValue {

	private boolean checked = false;

	/**
	 * @param checked the boolean value
	 */
	public InputValueBoolean(boolean checked) {
		this.checked = checked;
	}

	@Override
	public String toString() {
		return getValue();
	}

	/**
	 * @return the checked
	 */
	public boolean isChecked() {
		return checked;
	}

	/**
	 * @param checked the checked to set
	 */
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
}
