/**
 * Created Aug 13, 2008
 */
package com.crawljax.forms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.util.Helper;
import com.crawljax.util.XPathHelper;

/**
 * Handles form values and fills in the form input elements with random values of the defined
 * values.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id: FormHandler.java 6320 2009-12-25 20:53:02Z danny $
 */
public class FormHandler {
	private static final Logger LOGGER = Logger.getLogger(FormHandler.class.getName());

	private boolean randomFieldValue = false;
	private final EmbeddedBrowser browser;

	private static final double HALF = 0.5;

	/**
	 * Public constructor.
	 * 
	 * @param browser
	 *            the embedded browser.
	 */
	public FormHandler(EmbeddedBrowser browser) {
		this.browser = browser;
	}

	private static final String[] ALLOWED_INPUT_TYPES =
	        { "text", "radio", "checkbox", "password" };

	/**
	 * Fills in the element with the InputValues for input TODO: improve this by using WebDriver
	 * options?
	 * 
	 * @param element
	 * @param input
	 * @throws Exception
	 */
	private void setInputElementValue(Node element, FormInput input) {

		LOGGER.debug("INPUTFIELD: " + input.getName() + " (" + input.getType() + ")");
		if (element == null) {
			return;
		}
		if (input.getInputValues().iterator().hasNext()) {
			try {
				// fill in text fields, textareas, password fields and hidden
				// fields
				if (input.getType().toLowerCase().startsWith("text")
				        || input.getType().equalsIgnoreCase("password")
				        || input.getType().equalsIgnoreCase("hidden")) {
					String text = input.getInputValues().iterator().next().getValue();
					if (text.equals("")) {
						return;
					}
					String js = Helper.getJSGetElement(XPathHelper.getXpathExpression(element));
					js += "try{ATUSA_element.value='" + text + "';}catch(e){}";
					browser.executeJavaScript(js);
				}

				// check/uncheck checkboxes
				if (input.getType().equals("checkbox")) {
					for (InputValue inputValue : input.getInputValues()) {
						String js =
						        Helper.getJSGetElement(XPathHelper.getXpathExpression(element));
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

				// check radio button
				if (input.getType().equals("radio")) {
					for (InputValue inputValue : input.getInputValues()) {
						if (inputValue.isChecked()) {
							String js =
							        Helper.getJSGetElement(XPathHelper
							                .getXpathExpression(element));
							js += "try{ATUSA_element.checked=true;}catch(e){}";
							browser.executeJavaScript(js);
						}
					}
				}

				// select options
				if (input.getType().startsWith("select")) {
					for (InputValue inputValue : input.getInputValues()) {
						// if(browser.getDriver()==null){
						String js =
						        Helper.getJSGetElement(XPathHelper.getXpathExpression(element));
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
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}

	}

	/**
	 * @param dom
	 * @return all input element in dom
	 */
	private List<Node> getInputElements(Document dom) {
		List<Node> nodes = new ArrayList<Node>();
		try {
			NodeList nodeList = Helper.getElementsByXpath(dom, "//INPUT");
			List<String> allowedTypes = new ArrayList<String>(Arrays.asList(ALLOWED_INPUT_TYPES));

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node candidate = nodeList.item(i);
				Node typeAttribute = candidate.getAttributes().getNamedItem("type");
				if (typeAttribute == null
				        || (typeAttribute != null && allowedTypes.contains(typeAttribute
				                .getNodeValue()))) {
					nodes.add(nodeList.item(i));
				}
			}
			nodeList = Helper.getElementsByXpath(dom, "//TEXTAREA");
			for (int i = 0; i < nodeList.getLength(); i++) {
				nodes.add(nodeList.item(i));
			}
			nodeList = Helper.getElementsByXpath(dom, "//SELECT");
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
			dom = Helper.getDocument(browser.getDom());
			List<Node> nodes = getInputElements(dom);
			for (Node node : nodes) {
				FormInput formInput =
				        FormInputValueHelper.getFormInputWithDefaultValue(browser, node);
				if (formInput != null) {
					formInputs.add(formInput);
				}
			}
		} catch (Exception e) {
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
		Document dom;
		try {
			dom = Helper.getDocument(browser.getDom());
			for (FormInput input : formInputs) {
				LOGGER.debug("Filling in: " + input);
				setInputElementValue(FormInputValueHelper.getBelongingNode(input, dom), input);
			}
		} catch (Exception e) {
			LOGGER.warn("Could not handle form elements");
		}
	}

}
