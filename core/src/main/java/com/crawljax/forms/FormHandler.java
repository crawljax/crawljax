package com.crawljax.forms;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.eventablecondition.EventableCondition;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.configuration.CrawlRules;
import com.crawljax.core.exception.BrowserConnectionException;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.util.DomUtils;
import com.crawljax.util.XPathHelper;
import com.google.common.base.Enums;
import com.google.inject.assistedinject.Assisted;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.inject.Inject;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles form values and fills in the form input elements with random values of the defined
 * values.
 */
public class FormHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(FormHandler.class.getName());

	protected final EmbeddedBrowser browser;

	public static final int RANDOM_STRING_LENGTH = 8;

	protected final FormInputValueHelper formInputValueHelper;

	@Inject
	public FormHandler(@Assisted EmbeddedBrowser browser, CrawlRules config) {
		this.browser = browser;
		this.formInputValueHelper = FormInputValueHelper.getInstance(
				config.getInputSpecification(), config.getFormFillMode());
	}

	/**
	 * Fills in the element with the InputValues for input
	 *
	 * @param element the node element
	 * @param input   the input data
	 */
	protected void setInputElementValue(Node element, FormInput input) {

		LOGGER.debug("INPUTFIELD: {} ({})", input.getIdentification(), input.getType());
		if (element == null || input.getInputValues().isEmpty()) {
			return;
		}
		try {

			switch (input.getType()) {
				case TEXT:
				case TEXTAREA:
				case PASSWORD:
				case INPUT:
				case EMAIL:
				case NUMBER:
					handleText(input);
					break;
				case HIDDEN:
					handleHidden(input);
					break;
				case CHECKBOX:
					handleCheckBoxes(input);
					break;
				case RADIO:
					handleRadioSwitches(input);
					break;
				case SELECT:
					handleSelectBoxes(input);
			}

		} catch (ElementNotVisibleException e) {
			LOGGER.warn("Element not visible, input not completed.");
		} catch (BrowserConnectionException e) {
			throw e;
		} catch (RuntimeException e) {
			LOGGER.error("Could not input element values");
			throw e;
		}
	}

	private void handleCheckBoxes(FormInput input) {
		for (InputValue inputValue : input.getInputValues()) {
			boolean check = inputValue.isChecked();

			WebElement inputElement = browser.getWebElement(input.getIdentification());

			if (check && !inputElement.isSelected()) {
				inputElement.click();
			} else if (!check && inputElement.isSelected()) {
				inputElement.click();
			}
		}
	}

	private void resetRadioSwitches(FormInput input) {
		for (InputValue inputValue : input.getInputValues()) {
			if (inputValue.isChecked()) {
				WebElement inputElement = browser.getWebElement(input.getIdentification());
				if(inputElement!=null)
					((JavascriptExecutor)browser.getWebDriver()).executeScript("arguments[0].checked=false", inputElement);
			}
		}
	}
	
	private void handleRadioSwitches(FormInput input) {
		for (InputValue inputValue : input.getInputValues()) {
			if (inputValue.isChecked()) {
				WebElement inputElement = browser.getWebElement(input.getIdentification());
				inputElement.click();
			}
		}
	}

	private void resetSelectBoxes(FormInput input) {
		for (InputValue inputValue : input.getInputValues()) {
			WebElement inputElement = browser.getWebElement(input.getIdentification());
			inputElement.sendKeys(inputValue.getValue());
		}
	}
	
	private void handleSelectBoxes(FormInput input) {
		for (InputValue inputValue : input.getInputValues()) {
			WebElement inputElement = browser.getWebElement(input.getIdentification());
			inputElement.sendKeys(inputValue.getValue());
		}
	}
	
	/**
	 * Clear the input for given input text
	 * @param input
	 */
	private void resetText(FormInput input) {
		String text = input.getInputValues().iterator().next().getValue();
		if (null == text || text.length() == 0) {
			return;
		}
		WebElement inputElement = browser.getWebElement(input.getIdentification());
		inputElement.clear();
		inputElement.sendKeys(Keys.BACK_SPACE);
//		inputElement.sendKeys(text);
	}

	private void handleText(FormInput input) {
		String text = input.getInputValues().iterator().next().getValue();
		if (null == text || text.length() == 0) {
			return;
		}
		WebElement inputElement = browser.getWebElement(input.getIdentification());
		inputElement.clear();
		inputElement.sendKeys(text);
	}

	/**
	 * Enter information into the hidden input field.
	 *
	 * @param input The input to enter into the hidden field.
	 */
	private void handleHidden(FormInput input) {
		String text = input.getInputValues().iterator().next().getValue();
		if (null == text || text.length() == 0) {
			return;
		}
		WebElement inputElement = browser.getWebElement(input.getIdentification());
		JavascriptExecutor js = (JavascriptExecutor) browser.getWebDriver();
		js.executeScript("arguments[0].setAttribute(arguments[1], arguments[2]);", inputElement,
				"value", text);
	}

	/**
	 * @return all input element in dom
	 */
	private List<Node> getInputElements(Document dom) {
		List<Node> nodes = new ArrayList<>();
		try {
			NodeList nodeList = XPathHelper.evaluateXpathExpression(dom, "//INPUT");

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node candidate = nodeList.item(i);
				Node typeAttribute = candidate.getAttributes().getNamedItem("type");
				if (typeAttribute == null
						|| (Enums
						.getIfPresent(FormInput.InputType.class,
								typeAttribute.getNodeValue().toUpperCase())
						.isPresent())) {

					nodes.add(nodeList.item(i));
				}
			}
			nodeList = XPathHelper.evaluateXpathExpression(dom, "//TEXTAREA");
			for (int i = 0; i < nodeList.getLength(); i++) {
				nodes.add(nodeList.item(i));
			}
			nodeList = XPathHelper.evaluateXpathExpression(dom, "//SELECT");
			for (int i = 0; i < nodeList.getLength(); i++) {
				nodes.add(nodeList.item(i));
			}

			return nodes;
		} catch (XPathExpressionException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return nodes;
	}

	/**
	 * @return a list of form inputs.
	 */
	public List<FormInput> getFormInputs() {

		final List<FormInput> formInputs = new ArrayList<>();
		try {
			Document dom = DomUtils.asDocument(browser.getStrippedDom());
			List<Node> nodes = getInputElements(dom);
			for (Node node : nodes) {
				FormInput formInput =
						formInputValueHelper.getFormInputWithIndexValue(browser, node, 0);
				if (formInput != null) {
					formInputs.add(formInput);
				}
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return formInputs;
	}
	
	public List<FormInput> resetFormInputs(List<FormInput> formInputs) {
		ArrayList<FormInput> handled = new ArrayList<>();
		FormInput failing = null;
		try {
			Document dom = DomUtils.asDocument(browser.getStrippedDomWithoutIframeContent());
			for (FormInput input : formInputs) {
				failing = input;
				LOGGER.info("resetting : " + input.getIdentification().getValue());
				resetInputElementValue(formInputValueHelper.getBelongingNode(input, dom), input);
				handled.add(input);
				failing = null;
			}
		} catch (Exception e) {
			LOGGER.error("Could not reset form elements");
			LOGGER.error(e.getMessage());
		}
		if(failing==null) {
			handled.add(new FormInput(null, null));
		}
		else {
			handled.add(failing);
		}
		return handled;
	}

	protected void resetInputElementValue(Node element, FormInput input) {

		LOGGER.debug("INPUTFIELD: {} ({})", input.getIdentification(), input.getType());
		if (element == null || input.getInputValues().isEmpty()) {
			return;
		}
		try {

			switch (input.getType()) {
				case TEXT:
				case TEXTAREA:
				case PASSWORD:
				case INPUT:
				case EMAIL:
					resetText(input);
					break;
//				case HIDDEN:
//					resetHidden(input);
//					break;
				case CHECKBOX:
					LOGGER.info("Resetting checkbox{}", input);
					resetCheckBoxes(input);
					break;
				case RADIO:
					resetRadioSwitches(input);
					break;
//				case SELECT:
//					handleSelectBoxes(input);
			default:
				break;
			}

		} catch (ElementNotVisibleException e) {
			LOGGER.warn("Element not visible, input not completed.");
		} catch (BrowserConnectionException e) {
			throw e;
		} catch (RuntimeException e) {
			LOGGER.error("Could not input element values");
			throw e;
		}
	}


	private void resetCheckBoxes(FormInput input) {
		for (InputValue inputValue : input.getInputValues()) {
			boolean check = inputValue.isChecked();

			WebElement inputElement = browser.getWebElement(input.getIdentification());

			if (check && inputElement.isSelected()) {
				inputElement.click();
			}
//				else if (!check && !inputElement.isSelected()) {
//				inputElement.click();
//			}
		}
	}

	/**
	 * Fills in form/input elements.
	 *
	 * @param formInputs form input list.
	 * @return 
	 */
	public List<FormInput> handleFormElements(List<FormInput> formInputs) {
		ArrayList<FormInput> handled = new ArrayList<>();
		FormInput failing = null;
		try {
			Document dom = DomUtils.asDocument(browser.getStrippedDomWithoutIframeContent());
			for (FormInput input : formInputs) {
				failing = input;
				LOGGER.info("Filling in: " + input.getIdentification().getValue());
				Node belongingNode = formInputValueHelper.getBelongingNode(input, dom);
				setInputElementValue(belongingNode, input);
				if(belongingNode!=null) {
					String xpath = XPathHelper.getSkeletonXpath(belongingNode);
					Identification xpathId = new Identification(How.xpath, xpath);
					FormInput handledInput = new FormInput(input.getType(), xpathId);
					handledInput.inputValues(input.getInputValues());
					handled.add(handledInput);
				}
				else {
					handled.add(input);
				}
//				input.getIdentification().setHow(How.xpath);
//				input.getIdentification().setValue(XPathHelper.getSkeletonXpath(belongingNode));
				failing = null;
			}
		} catch (Exception e) {
			LOGGER.error("Could not handle form elements");
			LOGGER.error(e.getMessage());
		}
		if(failing==null) {
			handled.add(new FormInput(null, null));
		}
		else {
			handled.add(failing);
		}
		return handled;
	}

	/**
	 * @param sourceElement      the form element
	 * @param eventableCondition the belonging eventable condition for sourceElement
	 * @return a list with Candidate elements for the inputs.
	 */
	public List<CandidateElement> getCandidateElementsForInputs(Element sourceElement,
			EventableCondition eventableCondition) {

		return formInputValueHelper.getCandidateElementsForInputs(browser, sourceElement,
				eventableCondition);
	}

}
