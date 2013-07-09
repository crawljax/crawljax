package com.crawljax.forms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.eventablecondition.EventableCondition;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.state.Identification;
import com.crawljax.util.DomUtils;
import com.crawljax.util.XPathHelper;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;

/**
 * Helper class for FormHandler.
 */
public final class FormInputValueHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(FormInputValueHelper.class
	        .getName());

	private ImmutableMap<String, String> formFields;
	private ImmutableListMultimap<String, String> formFieldNames;
	private ImmutableListMultimap<String, String> fieldValues;

	private boolean randomInput;

	private static final int EMPTY = 0;

	/**
	 * @param inputSpecification
	 *            the input specification.
	 * @param randomInput
	 *            if random data should be used on the input fields.
	 */
	public FormInputValueHelper(InputSpecification inputSpecification, boolean randomInput) {

		this.formFieldNames = inputSpecification.getFormFieldNames();
		this.fieldValues = inputSpecification.getFormFieldValues();

		ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
		for (Entry<String, String> entry : formFieldNames.entries()) {
			builder.put(entry.getValue(), entry.getKey());
		}
		formFields = builder.build();

		this.randomInput = randomInput;
	}

	private Element getBelongingElement(Document dom, String fieldName) {
		List<String> names = getNamesForInputFieldId(fieldName);
		if (names != null) {
			for (String name : names) {
				String xpath = "//*[@name='" + name + "' or @id='" + name + "']";
				try {
					Node node = DomUtils.getElementByXpath(dom, xpath);
					if (node != null) {
						return (Element) node;
					}
				} catch (XPathExpressionException e) {
					LOGGER.debug(e.getLocalizedMessage(), e);
					// just try next
				}
			}
		}
		return null;
	}

	private int getMaxNumberOfValues(List<String> fieldNames) {
		int maxValues = 0;
		// check the maximum number of form inputValues
		for (String fieldName : fieldNames) {
			List<String> values = getValuesForName(fieldName);
			if (values != null && values.size() > maxValues) {
				maxValues = values.size();
			}
		}
		return maxValues;
	}

	/**
	 * @param browser
	 *            the browser instance
	 * @param sourceElement
	 *            the form elements
	 * @param eventableCondition
	 *            the belonging eventable condition for sourceElement
	 * @return a list with Candidate elements for the inputs
	 */
	public List<CandidateElement> getCandidateElementsForInputs(EmbeddedBrowser browser,
	        Element sourceElement, EventableCondition eventableCondition) {
		List<CandidateElement> candidateElements = new ArrayList<CandidateElement>();
		int maxValues = getMaxNumberOfValues(eventableCondition.getLinkedInputFields());

		if (maxValues == EMPTY) {
			LOGGER.warn("No input values found for element: "
			        + DomUtils.getElementString(sourceElement));
			return candidateElements;
		}

		Document dom;
		try {
			dom = DomUtils.asDocument(browser.getStrippedDomWithoutIframeContent());
		} catch (IOException e) {
			LOGGER.error("Catched IOException while parsing dom", e);
			return candidateElements;
		}

		// add maxValues Candidate Elements for every input combination
		for (int curValueIndex = 0; curValueIndex < maxValues; curValueIndex++) {
			List<FormInput> formInputsForCurrentIndex = new ArrayList<FormInput>();
			for (String fieldName : eventableCondition.getLinkedInputFields()) {
				Element element = getBelongingElement(dom, fieldName);
				if (element != null) {
					FormInput formInput =
					        getFormInputWithIndexValue(browser, element, curValueIndex);
					formInputsForCurrentIndex.add(formInput);
				} else {
					LOGGER.warn("Could not find input element for: " + fieldName);
				}
			}

			String id = eventableCondition.getId() + "_" + curValueIndex;
			sourceElement.setAttribute("atusa", id);

			// clone node inclusive text content
			Element cloneElement = (Element) sourceElement.cloneNode(false);
			cloneElement.setTextContent(DomUtils.getTextValue(sourceElement));

			CandidateElement candidateElement =
			        new CandidateElement(cloneElement,
			                XPathHelper.getXPathExpression(sourceElement),
			                formInputsForCurrentIndex);
			candidateElements.add(candidateElement);
		}
		return candidateElements;
	}

	/**
	 * @param input
	 *            the form input
	 * @param dom
	 *            the document
	 * @return returns the belonging node to input in dom
	 * @throws XPathExpressionException
	 *             if a failure is occurred.
	 */
	public Node getBelongingNode(FormInput input, Document dom) throws XPathExpressionException {

		Node result = null;

		switch (input.getIdentification().getHow()) {
			case xpath:
				result = DomUtils.getElementByXpath(dom, input.getIdentification().getValue());
				break;

			case id:
			case name:
				String xpath = "";
				String element = "";

				if (input.getType().equalsIgnoreCase("select")
				        || input.getType().equalsIgnoreCase("textarea")) {
					element = input.getType().toUpperCase();
				} else {
					element = "INPUT";
				}
				xpath =
				        "//" + element + "[@name='" + input.getIdentification().getValue()
				                + "' or @id='" + input.getIdentification().getValue() + "']";
				result = DomUtils.getElementByXpath(dom, xpath);
				break;

			default:
				LOGGER.info("Identification " + input.getIdentification()
				        + " not supported yet for form inputs.");
				break;

		}

		return result;
	}

	/**
	 * @param element
	 * @return returns the id of the element if set, else the name. If none found, returns null
	 * @throws Exception
	 */
	private Identification getIdentification(Node element) throws Exception {
		NamedNodeMap attributes = element.getAttributes();
		if (attributes.getNamedItem("id") != null) {
			return new Identification(Identification.How.id, attributes.getNamedItem("id")
			        .getNodeValue());
		} else if (attributes.getNamedItem("name") != null) {
			return new Identification(Identification.How.name, attributes.getNamedItem("name")
			        .getNodeValue());
		}

		// try to find the xpath
		String xpathExpr = XPathHelper.getXPathExpression(element);
		if (xpathExpr != null && !xpathExpr.equals("")) {
			return new Identification(Identification.How.xpath, xpathExpr);
		}

		return null;
	}

	/**
	 * @param browser
	 *            the current browser instance
	 * @param element
	 *            the element in the dom
	 * @return the first related formInput belonging to element in the browser
	 */
	public FormInput getFormInputWithDefaultValue(EmbeddedBrowser browser, Node element) {
		return getFormInput(browser, element, 0);
	}

	/**
	 * @param browser
	 *            the current browser instance
	 * @param element
	 *            the element in the dom
	 * @param indexValue
	 *            the i-th specified value. if i>#values, first value is used
	 * @return the specified value with index indexValue for the belonging elements
	 */
	public FormInput getFormInputWithIndexValue(EmbeddedBrowser browser, Node element,
	        int indexValue) {
		return getFormInput(browser, element, indexValue);
	}

	private FormInput getFormInput(EmbeddedBrowser browser, Node element, int indexValue) {
		Identification identification;
		try {
			identification = getIdentification(element);
			if (identification == null) {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
		// CHECK
		String id = fieldMatches(identification.getValue());
		FormInput input = new FormInput();
		input.setType(getElementType(element));
		input.setIdentification(identification);
		Set<InputValue> values = new HashSet<InputValue>();

		if (id != null && fieldValues.containsKey(id)) {
			// TODO: make multiple selection for list available
			// add defined value to element
			String value;
			if (indexValue == 0 || fieldValues.get(id).size() == 1
			        || indexValue + 1 > fieldValues.get(id).size()) {
				// default value
				value = fieldValues.get(id).get(0);
			} else if (indexValue > 0) {
				// index value
				value = fieldValues.get(id).get(indexValue);
			} else {
				// random value
				value =
				        fieldValues.get(id).get(
				                new Random().nextInt(fieldValues.get(id).size() - 1));
			}

			if (input.getType().equals("checkbox") || input.getType().equals("radio")) {
				// check element
				values.add(new InputValue(value, value.equals("1")));
			} else {
				// set value of text input field
				values.add(new InputValue(value, true));
			}

			input.setInputValues(values);
		} else {

			if (this.randomInput) {
				return browser.getInputWithRandomValue(input);
			}
			// field is not specified, lets try a random value

		}
		return input;
	}

	private String fieldMatches(String fieldName) {
		for (String field : formFields.keySet()) {
			Pattern p = Pattern.compile(field, Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(fieldName);
			if (m.matches()) {
				return formFields.get(field);
			}
		}
		return null;
	}

	private List<String> getValuesForName(String inputFieldId) {
		if (!fieldValues.containsKey(inputFieldId)) {
			return null;
		}
		return fieldValues.get(inputFieldId);
	}

	private List<String> getNamesForInputFieldId(String inputFieldId) {
		if (!formFieldNames.containsKey(inputFieldId)) {
			return null;
		}
		return formFieldNames.get(inputFieldId);
	}

	private String getElementType(Node node) {
		if (node.getAttributes().getNamedItem("type") != null) {
			return node.getAttributes().getNamedItem("type").getNodeValue().toLowerCase();
		} else if (node.getNodeName().equalsIgnoreCase("input")) {
			return "text";
		} else {
			return node.getNodeName().toLowerCase();
		}
	}

}
