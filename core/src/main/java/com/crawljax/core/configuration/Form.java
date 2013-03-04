package com.crawljax.core.configuration;

/**
 * Defines a form which is a collection of input fields A form is used to assign multiple values to
 * form input fields. A CrawlElement is linked to the form via
 * {@link InputSpecification#beforeClickTag()} The HTML element that matches the CrawlElement
 * properties will be referred to as the submit button. When Crawljax finds the submit button it
 * sets the form data N number of times, where N is the largest number of values specified for the
 * input fields in this form. Thus when input field A has three values defined and input field B two
 * values, the form fields will be filled in three times and the submit button will be clicked three
 * times. The first time Crawljax clicks the submit it fills in the first defined values, the second
 * time, the second defined values and to on. When Crawljax fills in the form for the i-th time and
 * an input field has less than i values specified, its first value will be used. If there is an
 * element clicked which is not the submit button, the first values of the specified input fields
 * are filled in. EXAMPLE: <code>
 * InputSpecification input = new InputSpecification();
 * Form contactForm = new Form();
 * contactForm.field("name").setValues("Bob", "Alice", "John");
 * contactForm.field("age").setValues("25", "44");
 * input.setValuesInForm(contactForm).beforeClickTag("button").withText("Save");
 * </code> When Crawljax finds the Save Button (the submit button), the form will be filled in three
 * times with the values: 1) name=Bob; age=25 2) name=Alice; age=44 3) name=John; 25 and the submit
 * button is clicked three times. For every other HTML element that is clicked, the first defined
 * value is filled in e.g.: name=Bob; age=25.
 * 
 * @author DannyRoest@gmail.com (Danny Roest)
 */
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Form configuration.
 */
public class Form {

	private final List<FormInputField> inputFields = new ArrayList<FormInputField>();
	private FormAction formAction;

	/**
	 * Specifies an input field to assign a value to. Crawljax first tries to match the found HTML
	 * input element's id and then the name attribute.
	 * 
	 * @param fieldName
	 *            the id or name attribute of the input field
	 * @return an InputField
	 */
	public FormInputField field(String fieldName) {
		FormInputField inputField = new FormInputField();
		inputField.setFieldName(fieldName);
		this.inputFields.add(inputField);
		return inputField;
	}

	/**
	 * @return the inputFields
	 */
	protected ImmutableList<FormInputField> getInputFields() {
		return ImmutableList.copyOf(inputFields);
	}

	/**
	 * @param formAction
	 *            The form action.
	 */
	protected void setFormAction(FormAction formAction) {
		this.formAction = formAction;
	}

	/**
	 * @return the formAction
	 */
	protected FormAction getFormAction() {
		return formAction;
	}

	/**
	 * @return the crawlTag
	 */
	protected CrawlElement getCrawlElement() {
		CrawlElement crawlTag = formAction.getCrawlElement();
		List<String> inputFieldIds = new ArrayList<String>();
		for (FormInputField inputField : this.inputFields) {
			inputFieldIds.add(inputField.getId());
		}
		crawlTag.addInputFieldIds(inputFieldIds);
		return crawlTag;
	}

	/**
	 * @param inputField
	 *            The inputfield.
	 */
	protected void addInputField(FormInputField inputField) {
		this.inputFields.add(inputField);
	}

	@Override
	public String toString() {
		return formAction.getCrawlElement().toString() + " sets " + inputFields.toString();
	}

}
