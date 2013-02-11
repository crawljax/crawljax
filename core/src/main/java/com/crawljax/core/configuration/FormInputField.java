package com.crawljax.core.configuration;

/**
 * Represents a form input field NOTE: In general FormInputField is not designed to be instantiated
 * directly. For example: <input type="text" name="foo" /> <input type="checkbox" id="bar" />
 * FormInputSpecification input = new FormInputSpecification() Form form = new Form();
 * input.field("foo").setValues("Crawljax", "Crawler", "Automatic");
 * input.field("bar").setValues(true, true, false);
 * input.setValuesInForm(contactForm).beforeClickTag("a").withText("Search"); Crawljax will set the
 * text value of the foo text field and the bar checkbox three times when clicked on the anchor
 * element with the test Search to: 1) foo=Crawljax; bar=checked 2) foo=Crawler; bar=checked 3)
 * foo=Automatic; bar=unchecked
 * 
 * @see Form
 * @see Form#field(String)
 * @author DannyRoest@gmail.com (Danny Roest)
 */
public class FormInputField extends InputField {

	/**
	 * Sets the valus of this input field with a text input. Applicable to all form elements except
	 * checkboxes and a radio buttons.
	 * 
	 * @param values
	 *            Values to set.
	 * @return this FormInputField
	 */
	public FormInputField setValues(String... values) {
		for (String value : values) {
			this.setValue(value);
		}
		return this;
	}

	/**
	 * Sets the values of this input field. Only Applicable checkboxes and a radio buttons.
	 * 
	 * @param values
	 *            Values to set.
	 * @return this FormInputField
	 */
	public FormInputField setValues(boolean... values) {
		for (boolean value : values) {
			if (value) {
				this.setValue("1");
			} else {
				this.setValue("0");
			}
		}
		return this;
	}

}
