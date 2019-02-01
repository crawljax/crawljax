package com.crawljax.core.configuration;

import com.crawljax.core.state.Identification;
import com.crawljax.forms.FormInput;
import com.crawljax.forms.FormInput.InputType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Specifies values for form input fields The user specifies the ids or names of the input fields.
 * When Crawljax enters a new state it scans the DOM for input fields and tries to match the field
 * ids/names to the specified input fields. When there is a match, Crawljax enters the specified
 * value EXAMPLE:
 * HTML:
 * <pre>
 * <code>
 * Name: &lt;input type="text" id="name" /&gt;&lt;br /&gt;
 * Phone: &lt;input name="phone" /&gt;&lt;br /&gt;
 * Mobile: &lt;input name="mobile" /&gt;&lt;br /&gt;
 * Agree to licence: &lt;input id="agreelicence" type="checkbox" /&gt;&lt;br /&gt;
 * Other:content
 * </code>
 * </pre>
 * JAVA:
 * <pre>
 * <code>
 * InputSpecification input = new InputSpecification();
 * //for fields with text
 * input.field("name").setValue("John Doe");
 * input.field("phone", "mobile").setValue("1234567890");
 * //for checkboxes
 * input.field("agreelicence").setValue(true);
 * </code>
 * </pre>
 * Crawljax will set Name, Phone, Mobile, and Agree values. It will enter a random string in the
 * Other field if enabled in {@link CrawljaxConfiguration}
 */
public final class InputSpecification {

	private final Map<Identification, FormInput> formInputs =
			new HashMap<>();

	// private final List<InputField> inputFields = Lists.newLinkedList();
	private final List<Form> forms = Lists.newLinkedList();

	public FormInput inputField(InputType type, Identification identification) {
		FormInput input = new FormInput(type, identification);
		this.formInputs.put(input.getIdentification(), input);
		return input;
	}

	public FormInput inputField(FormInput input) {
		this.formInputs.put(input.getIdentification(), input);
		return input;
	}

	public Map<Identification, FormInput> getFormInputs() {
		return formInputs;
	}

	/**
	 * Links the form with an HTML element which can be clicked.
	 *
	 * @param form the collection of the input fields
	 * @return a FormAction
	 * @see Form
	 */
	public FormAction setValuesInForm(Form form) {
		FormAction formAction = new FormAction();
		form.setFormAction(formAction);
		this.forms.add(form);
		return formAction;
	}

	public ImmutableList<Form> getForms() {
		return ImmutableList.copyOf(this.forms);
	}

	/**
	 * @return List of crawlelements.
	 */
	public ImmutableList<CrawlElement> getCrawlElements() {
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
