package com.crawljax.core.configuration;

import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;

/**
 * Specifies values for form input fields The user specifies the ids or names of the input fields.
 * When Crawljax enters a new state it scans the DOM for input fields and tries to match the field
 * ids/names to the specified input fields. When there is a match, Crawljax enters the specified
 * value EXAMPLE: HTML: <code>
 * Name: <input type="text" id="name" /><br />
 * Phone: <input name="phone" /><br />
 * Mobile: <input name="mobile" /><br />
 * Agree to licence: <input id="agreelicence" type="checkbox" /><br />
 * Other:content
 * </code> JAVA: <code>
 * InputSpecification input = new InputSpecification();
 * //for fields with text
 * input.field("name").setValue("John Doe");
 * input.field("phone", "mobile").setValue("1234567890");
 * //for checkboxes
 * input.field("agreelicence").setValue(true);
 * </code> Crawljax will set Name, Phone, Mobile, and Agree values. It will enter a random string in
 * the Other field if enabled in {@link CrawlSpecification}
 * 
 * @author DannyRoest@gmail.com (Danny Roest)
 * @version $Id$
 */
public final class InputSpecification {

	private final List<InputField> inputFields = Lists.newLinkedList();
	private final List<Form> forms = Lists.newLinkedList();

	/**
	 * Specifies an input field to assign a value to. Crawljax first tries to match the found HTML
	 * input element's id and then the name attribute.
	 * 
	 * @param fieldName
	 *            the id or name attribute of the input field
	 * @return an InputField
	 */
	public InputField field(String fieldName) {
		InputField inputField = new InputField();
		inputField.setFieldName(fieldName);
		this.inputFields.add(inputField);
		return inputField;
	}

	/**
	 * Specifies input fields to assign one value to. Crawljax first tries to match the found HTML
	 * input element's id and then the name attribute.
	 * 
	 * @param fieldNames
	 *            the ids or names of the input fields
	 * @return an InputField
	 */
	public InputField fields(String... fieldNames) {
		InputField inputField = new InputField();
		inputField.setFieldNames(fieldNames);
		this.inputFields.add(inputField);
		return inputField;
	}

	/**
	 * Links the form with an HTML element which can be clicked.
	 * 
	 * @see Form
	 * @param form
	 *            the collection of the input fields
	 * @return a FormAction
	 */
	public FormAction setValuesInForm(Form form) {
		FormAction formAction = new FormAction();
		form.setFormAction(formAction);
		this.forms.add(form);
		return formAction;
	}

	// hidden

	private void addProperty(PropertiesConfiguration config, InputField inputField) {
		String fields = ConfigurationHelper.listToString(inputField.getFieldNames());
		String values =
		        ConfigurationHelper.listToStringEmptyStringAllowed(inputField.getFieldValues());
		config.addProperty(inputField.getId() + ".fields", fields);
		config.addProperty(inputField.getId() + ".values", values);
	}

	/**
	 * @return The properties configuration object.
	 */
	protected PropertiesConfiguration getConfiguration() {
		PropertiesConfiguration config = new PropertiesConfiguration();
		for (Form form : this.forms) {
			for (FormInputField inputField : form.getInputFields()) {
				addProperty(config, inputField);
			}
		}
		for (InputField inputField : inputFields) {
			addProperty(config, inputField);
		}

		return config;
	}

	/**
	 * @return List of crawlelements.
	 */
	protected ImmutableList<CrawlElement> getCrawlElements() {
		Builder<CrawlElement> builder = ImmutableList.builder();
		for (Form form : this.forms) {
			CrawlElement crawlTag = form.getCrawlElement();
			if (crawlTag != null) {
				builder.add(crawlTag);
			}
		}
		return builder.build();
	}

}
