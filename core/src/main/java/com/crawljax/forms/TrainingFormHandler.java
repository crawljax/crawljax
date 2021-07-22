package com.crawljax.forms;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.configuration.CrawlRules;
import com.crawljax.core.state.Identification;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Creates form value templates by pausing the crawl and allowing the user to manually enter form
 * formInputs.
 *
 * @author qhanam
 */
public class TrainingFormHandler extends FormHandler {

	@SuppressWarnings("unused")
	private static final Logger LOGGER =
			LoggerFactory.getLogger(TrainingFormHandler.class.getName());

	/**
	 * The HTML to display when the crawl is paused for form input.
	 */
	private static final String MODAL_HTML =
			"<div class='modal-content' style='position:fixed;bottom:0;background-color:#fefefe;width:100%;'><div class='modal-header' style='.padding:2px 16px;background-color:#5cb85c;color:white;'><span class='close'>&times</span><h2>Crawl Paused</h2></div><div class='modal-body' style='padding:2px 16px;'><p>The crawl has encountered form inputs which have not been trained. Please enter all input data for the page and hit 'F1' when finished.</p></div><div class='modal-footer' style='padding:2px 16px;background-color:#5cb85c;color:white';><h3>Crawljax Training Alert</h3></div></div>";

	/**
	 * Keeps track of the form data manually entered during training.
	 **/
	private static Set<Identification> visited = new HashSet<>();

	@Inject
	public TrainingFormHandler(@Assisted EmbeddedBrowser browser, CrawlRules config) {
		super(browser, config);
	}

	/**
	 * Fills in form/input elements.
	 * Because we write to the singleton  @code{FormInputValueHelper}, this method
	 * must be mutually exclusive. In practice, this probably has no effect because
	 * it is unlikely that someone would train the crawler using multiple browser
	 * threads.
	 *
	 * @param formInputs form input list.
	 * @return 
	 */
	public synchronized List<FormInput> handleFormElements(List<FormInput> formInputs) {

		/* If there are no form elements, there is nothing to do. */
		if (formInputs.isEmpty())
//			return true;
			return new ArrayList<>();

		/* First fill in the data from previous sessions through
		 * InputSpecification or forms.json */
		super.handleFormElements(formInputs);

		/* If there are form inputs that we haven't seen, pause the crawl and
		 * request user input. */
		if (incomplete(formInputs))
			pauseAndTrainModal(formInputs);

//		return true;
		return formInputs;
	}

	/**
	 * Checks if the forms on the page are complete.
	 * To invoke a training pause, an element must exist in the DOM such that:
	 * 1. The element is not in the map of visited elements.
	 * 2. The element is visible
	 *
	 * @return true if there are input elements in the DOM that the trainer
	 * has not seen yet.
	 */
	private boolean incomplete(List<FormInput> formInputs) {

		for (FormInput input : formInputs) {

			/* Is the element in the map of visited elements? */
			if (!visited.contains(input.getIdentification())) {

				/* Is the element visible? */
				WebElement inputElement = browser.getWebElement(input.getIdentification());
				if (!inputElement.getAttribute("type").equalsIgnoreCase("hidden")
						&& inputElement.isDisplayed())
					return true;

			}

		}

		return false;

	}

	/**
	 * Pauses the crawl for manual form input. After the user is finished, input formInputs are
	 * stored and shared across handlers in the static map {@code TrainingFormHandler.formInputs}.
	 *
	 * @param formInputs The input elements in the current DOM.
	 */
	private void pauseAndTrainModal(List<FormInput> formInputs) {

		/* If we are doing a training crawl, allow the user to edit remaining forms. */
		WebDriver driver = browser.getWebDriver();
		driver.manage().timeouts().setScriptTimeout(180, TimeUnit.SECONDS);

		String js = "";

		/* Inject the modal notification. */
		js += "function addStyleString(str) {";
		js += "	var node = document.createElement('style');";
		js += "	node.innerHTML = str;";
		js += "	document.body.appendChild(node);";
		js += "}";

		js += "function addHTMLString(str) {";
		js += "	var modal = document.createElement('div');";
		js += "	modal.setAttribute('id', 'myModal');";
		js += "	modal.setAttribute('class', 'modal');";
		js +=
				" modal.setAttribute('style', 'display:none;position:fixed;z-index:1;left:0;top:0;width:100%;height:100%;overflow:auto;background-color:rgb(0,0,0);background-color:rgba(0,0,0,0.4)');";
		js += "	modal.innerHTML = str;";
		js += "	document.body.appendChild(modal);";
		js += "}";

		js += "addHTMLString(\"" + MODAL_HTML + "\");";

		js += "var modal = document.getElementById('myModal');";
		js += "var span = document.getElementsByClassName('close')[0];";

		js += "span.onclick = function() {";
		js += "	modal.style.display = 'none';";
		js += "};";

		js += "window.onclick = function(event) {";
		js += "	if (event.target == modal) {";
		js += "		modal.style.display = 'none';";
		js += "	}";
		js += "};";

		js += "modal.style.display = 'block';";

		/* Inject the callback and continuation keys. */
		js += "var callback = arguments[arguments.length - 1];";
		js += "var title = document.title;";
		js += "document.onkeydown = function(e) {";
		js += "if(e.keyCode === 112) {";
		js += "document.title = title;";
		js += "setTimeout(callback, 1);";
		js += "}";
		js += "}";

		JavascriptExecutor executor = (JavascriptExecutor) driver;
		executor.executeAsyncScript(js);

		/*
		 * Save each FormInput element so the user does not have to enter them again.
		 */
		for (FormInput input : formInputs) {

			/* Create the InputValue for this element. */
			InputValue inputValue = getInputValue(input);

			/* Read the value from the DOM. */
			Set<InputValue> inputValues = new HashSet<>();
			inputValues.add(inputValue);

			/* The DOM value is the new input value. */
			input.inputValues(inputValues);

			/* Label the element as visited. */
			visited.add(input.getIdentification());

			/* Add this input value to the set. */
			formInputValueHelper.addTrainingInput(input.getIdentification(), input);

		}

	}

	/**
	 * Generates the InputValue for the form input by inspecting the current
	 * value of the corresponding WebElement on the DOM.
	 *
	 * @return The current InputValue for the element on the DOM.
	 */
	private InputValue getInputValue(FormInput input) {

		/* Get the DOM element from Selenium. */
		WebElement inputElement = browser.getWebElement(input.getIdentification());

		switch (input.getType()) {
			case TEXT:
			case PASSWORD:
			case HIDDEN:
			case SELECT:
			case TEXTAREA:
				return new InputValue(inputElement.getAttribute("value"));
			case RADIO:
			case CHECKBOX:
			default:
				String value = inputElement.getAttribute("value");
				Boolean checked = inputElement.isSelected();
				return new InputValue(value, checked);
		}

	}

}