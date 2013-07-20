package com.crawljax.forms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.eventablecondition.EventableCondition;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.configuration.CrawlRules;
import com.crawljax.core.exception.BrowserConnectionException;
import com.crawljax.util.DomUtils;
import com.crawljax.util.XPathHelper;
import com.google.inject.assistedinject.Assisted;

/**
 * Handles form values and fills in the form input elements with random values of the defined
 * values.
 */
public class FormHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(FormHandler.class.getName());

	private boolean randomFieldValue = false;
	private final EmbeddedBrowser browser;

	public static final int RANDOM_STRING_LENGTH = 8;

	private static final double HALF = 0.5;

	private final FormInputValueHelper formInputValueHelper;

	@Inject
	public FormHandler(@Assisted EmbeddedBrowser browser, CrawlRules config) {
		this.browser = browser;
		this.formInputValueHelper =
		        new FormInputValueHelper(config.getInputSpecification(),
		                config.isRandomInputInForms());
	}

	private static final String[] ALLOWED_INPUT_TYPES =
	{ "text", "radio", "checkbox", "password" };

	/**
	 * Fills in the element with the InputValues for input TODO: improve this by using WebDriver
	 * options?
	 */
	private void setInputElementValue(Node element, FormInput input) {

		LOGGER.debug("INPUTFIELD: {} ({})", input.getIdentification(), input.getType());
		if (element == null || input.getInputValues().isEmpty()) {
			return;
		}
		try {
			if (input.getType().toLowerCase().startsWith("text")
			        || input.getType().equalsIgnoreCase("password")
			        || input.getType().equalsIgnoreCase("hidden")) {
				handleText(element, input);
			} else if ("checkbox".equals(input.getType())) {
				handleCheckBoxes(element, input);
			} else if (input.getType().equals("radio")) {
				handleRadioSwitches(element, input);
			} else if (input.getType().startsWith("select")) {
				handleSelectBoxes(element, input);
			}
		} catch (BrowserConnectionException e) {
			throw e;
		} catch (RuntimeException e) {
			LOGGER.error("Could not input element values", e);
		}
	}

	private void handleCheckBoxes(Node element, FormInput input) {
		for (InputValue inputValue : input.getInputValues()) {
			String js =
			        DomUtils.getJSGetElement(XPathHelper.getXPathExpression(element));
			boolean check;
			if (!randomFieldValue) {
				check = inputValue.isChecked();
			} else {

				check = Math.random() >= HALF;
			}
			String value;
			if (check) {
				value = "true";
			} else {
				value = "false";
			}
			js += "try{ATUSA_element.checked=" + value + ";}catch(e){}";
			browser.executeJavaScript(js);

		}
	}

	private void handleRadioSwitches(Node element, FormInput input) {
		for (InputValue inputValue : input.getInputValues()) {
			if (inputValue.isChecked()) {
				String js =
				        DomUtils.getJSGetElement(XPathHelper
				                .getXPathExpression(element));
				js += "try{ATUSA_element.checked=true;}catch(e){}";
				browser.executeJavaScript(js);
			}
		}
	}

	private void handleSelectBoxes(Node element, FormInput input) {
		for (InputValue inputValue : input.getInputValues()) {
			String js =
			        DomUtils.getJSGetElement(XPathHelper.getXPathExpression(element));
			js +=
			        "try{" + "for(i=0; i<ATUSA_element.options.length; i++){"
			                + "if(ATUSA_element.options[i].value=='"
			                + inputValue.getValue()
			                + "' || ATUSA_element.options[i].text=='"
			                + inputValue.getValue() + "'){"
			                + "ATUSA_element.options[i].selected=true;" + "break;"
			                + "}" + "};" + "}catch(e){}";
			browser.executeJavaScript(js);
		}
	}

	private void handleText(Node element, FormInput input) {
		String text = input.getInputValues().iterator().next().getValue();
		if ("".equals(text)) {
			return;
		}
		String js = DomUtils.getJSGetElement(XPathHelper.getXPathExpression(element));
		js += "try{ATUSA_element.value='" + text + "';}catch(e){}";
		browser.executeJavaScript(js);
	}

	/**
	 * @return all input element in dom
	 */
	private List<Node> getInputElements(Document dom) {
		List<Node> nodes = new ArrayList<Node>();
		try {
			NodeList nodeList = XPathHelper.evaluateXpathExpression(dom, "//INPUT");
			List<String> allowedTypes = new ArrayList<String>(Arrays.asList(ALLOWED_INPUT_TYPES));

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node candidate = nodeList.item(i);
				Node typeAttribute = candidate.getAttributes().getNamedItem("type");
				if (typeAttribute == null
				        || (allowedTypes.contains(typeAttribute.getNodeValue()))) {
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
		List<FormInput> formInputs = new ArrayList<FormInput>();
		Document dom;
		try {
			dom = DomUtils.asDocument(browser.getStrippedDom());
			List<Node> nodes = getInputElements(dom);
			for (Node node : nodes) {
				FormInput formInput =
				        formInputValueHelper.getFormInputWithDefaultValue(browser, node);
				if (formInput != null) {
					formInputs.add(formInput);
				}
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return formInputs;
	}

	/**
	 * Handle form elements.
	 * 
	 * @throws Exception
	 *             the exception.
	 */
	public void handleFormElements() throws Exception {
		handleFormElements(getFormInputs());
	}

	/**
	 * Fills in form/input elements.
	 * 
	 * @param formInputs
	 *            form input list.
	 */
	public void handleFormElements(List<FormInput> formInputs) {
		try {
			Document dom = DomUtils.asDocument(browser.getStrippedDomWithoutIframeContent());
			for (FormInput input : formInputs) {
				LOGGER.debug("Filling in: " + input);
				setInputElementValue(formInputValueHelper.getBelongingNode(input, dom), input);
			}
		} catch (IOException | XPathExpressionException e) {
			LOGGER.error(e.getMessage(), e);
		}

	}

	/**
	 * @param sourceElement
	 *            the form element
	 * @param eventableCondition
	 *            the belonging eventable condition for sourceElement
	 * @return a list with Candidate elements for the inputs.
	 */
	public List<CandidateElement> getCandidateElementsForInputs(Element sourceElement,
	        EventableCondition eventableCondition) {

		return formInputValueHelper.getCandidateElementsForInputs(browser, sourceElement,
		        eventableCondition);
	}

}
