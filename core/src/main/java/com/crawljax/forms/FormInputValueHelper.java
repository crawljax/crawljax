package com.crawljax.forms;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.eventablecondition.EventableCondition;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.configuration.CrawlRules.FormFillMode;
import com.crawljax.core.configuration.Form;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.state.Identification;
import com.crawljax.forms.FormInput.InputType;
import com.crawljax.util.DomUtils;
import com.crawljax.util.XPathHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Singleton helper class for FormHandler.
 */
public final class FormInputValueHelper {

	private static final Logger LOGGER =
			LoggerFactory.getLogger(FormInputValueHelper.class.getName());

	private static final String FORMS_JSON_FILE = "forms.json";

	private final Map<Identification, FormInput> formInputs;

	private FormFillMode formFillMode;

	private static final int EMPTY = 0;

	private static FormInputValueHelper instance = null;

	/**
	 * Creates or returns the instance of the helper class.
	 *
	 * @param inputSpecification the input specification.
	 * @param formFillMode       if random data should be used on the input fields.
	 * @return The singleton instance.
	 */
	public static synchronized FormInputValueHelper getInstance(
			InputSpecification inputSpecification, FormFillMode formFillMode) {
		if (instance == null)
			instance = new FormInputValueHelper(inputSpecification,
					formFillMode);
		return instance;
	}

	/**
	 * Removes the instance so another must be built. Used only to reset the state in between JUnit
	 * tests.
	 */
	public static void reset() {
		instance = null;
	}

	/**
	 * @param inputSpecification the input specification.
	 * @param formFillMode       if random data should be used on the input fields.
	 */
	private FormInputValueHelper(InputSpecification inputSpecification,
			FormFillMode formFillMode) {

		this.formFillMode = formFillMode;

		// add the free-floating inputs (without Forms)
		this.formInputs = inputSpecification.getFormInputs();

		// add the inputs defined inside Forms too
		for (Form form : inputSpecification.getForms()) {
			for (FormInput input : form.getFormInputs()) {
				this.formInputs.put(input.getIdentification(), input);
			}
		}

	}

	private int getMaxNumberOfValues(List<FormInput> fieldNames) {
		int maxValues = 0;
		// check the maximum number of form inputValues
		for (FormInput fieldName : fieldNames) {
			Set<InputValue> values = fieldName.getInputValues();// getValuesForName(fieldName);
			if (values != null && values.size() > maxValues) {
				maxValues = values.size();
			}
		}
		return maxValues;
	}

	/**
	 * @param browser            the browser instance
	 * @param sourceElement      the form elements
	 * @param eventableCondition the belonging eventable condition for sourceElement
	 * @return a list with Candidate elements for the inputs
	 */
	public List<CandidateElement> getCandidateElementsForInputs(EmbeddedBrowser browser,
			Element sourceElement, EventableCondition eventableCondition) {
		List<CandidateElement> candidateElements = new ArrayList<>();
		int maxValues = getMaxNumberOfValues(eventableCondition.getLinkedInputFields());

		if (maxValues == EMPTY) {
			LOGGER.warn("No input values found for element: "
					+ DomUtils.getElementString(sourceElement));
			return candidateElements;
		}

		try {
			final Document dom =
					DomUtils.asDocument(browser.getStrippedDomWithoutIframeContent());

			// add maxValues Candidate Elements for every input combination
			for (int curValueIndex = 0; curValueIndex < maxValues; curValueIndex++) {
				List<FormInput> formInputsForCurrentIndex = new ArrayList<>();
				for (FormInput input : eventableCondition.getLinkedInputFields()) {
					try {
						Element element = (Element) getBelongingNode(input, dom);
						if (element != null) {
							FormInput formInput =
									getFormInputWithIndexValue(browser, element, curValueIndex);
							formInputsForCurrentIndex.add(formInput);
						} else {
							LOGGER.warn("Could not find input element for: " + input);
						}
					} catch (XPathExpressionException e) {
						LOGGER.warn("Could not find input element for: " + input);
						LOGGER.error(e.getMessage(), e);
					}
				}

				String id = eventableCondition.getId() + "_" + curValueIndex;
				//sourceElement.setAttribute("atusa", id);

				// clone node inclusive text content
				Element cloneElement = (Element) sourceElement.cloneNode(false);
				cloneElement.setTextContent(DomUtils.getTextValue(sourceElement));

				//TODO: Why is this cloning done?
				
//				CandidateElement candidateElement = new CandidateElement(cloneElement,
				CandidateElement candidateElement = new CandidateElement(sourceElement,
						XPathHelper.getXPathExpression(sourceElement), formInputsForCurrentIndex);
				candidateElements.add(candidateElement);
			}
		} catch (IOException e) {
			LOGGER.error("Caught IOException while parsing DOM", e);
			return candidateElements;
		}
		return candidateElements;
	}

	/**
	 * @param input the form input
	 * @param dom   the document
	 * @return returns the belonging node to input in dom
	 * @throws XPathExpressionException if a failure is occurred.
	 */
	public Node getBelongingNode(FormInput input, Document dom) throws XPathExpressionException {

		Node result = null;

		switch (input.getIdentification().getHow()) {
			case xpath:
				result = DomUtils.getElementByXpath(dom, input.getIdentification().getValue());
				break;

			case id: // id and name are handled the same
			case name:
				String xpath = "";
				String element = "";

				if (input.getType().equals(InputType.SELECT)
						|| input.getType().equals(InputType.TEXTAREA)) {
					element = input.getType().toString().toUpperCase();
				} else {
					element = "INPUT";
				}
				xpath = "//" + element + "[@name='" + input.getIdentification().getValue()
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
	 * @return returns the id of the element if set, else the name. If none found, returns xpath
	 */
	private Identification getIdentification(Node element) {
		NamedNodeMap attributes = element.getAttributes();
		if (attributes.getNamedItem("id") != null
				&& formFillMode != FormFillMode.XPATH_TRAINING) {
			return new Identification(Identification.How.id,
					attributes.getNamedItem("id").getNodeValue());
		} else if (attributes.getNamedItem("name") != null
				&& formFillMode != FormFillMode.XPATH_TRAINING) {
			return new Identification(Identification.How.name,
					attributes.getNamedItem("name").getNodeValue());
		}

		// try to find the xpath
		String xpathExpr = XPathHelper.getXPathExpression(element);
		if (xpathExpr != null && !xpathExpr.equals("")) {
			return new Identification(Identification.How.xpath, xpathExpr);
		}

		return null;
	}

	/**
	 * Add the training data to the form input map.
	 *
	 * @param identification The identifier.
	 * @param input          The form input.
	 */
	public void addTrainingInput(
			Identification identification, FormInput input) {
		this.formInputs.put(identification, input);
	}

	/**
	 * Get the form input for the DOM element.
	 *
	 * @param identification specifies a DOM element
	 * @return the form input for the DOM element.
	 */
	public FormInput getFormInput(Identification identification) {
		return this.formInputs.get(identification);
	}

	/**
	 * @param browser    the current browser instance
	 * @param element    the element in the DOM
	 * @param indexValue the i-th specified value. if i&gt;#values, first value is used
	 * @return the specified value with index indexValue for the belonging elements
	 */
	public FormInput getFormInputWithIndexValue(EmbeddedBrowser browser, Node element,
			int indexValue) {
		return getFormInput(browser, element, indexValue);
	}

	private FormInput getFormInput(EmbeddedBrowser browser, Node element, int indexValue) {

		FormInput matchedInput = formInputMatchingNode(element);
		if (matchedInput != null) {
			return matchedInput;
		} else {
			try {
				FormInput input =
						new FormInput(getFormInputType(element), getIdentification(element));
				switch (this.formFillMode) {
					case RANDOM:
						// field is not specified, lets try a random value

						return browser.getInputWithRandomValue(input);
					case TRAINING:
					case XPATH_TRAINING:
						return input;

					default:
						break;
				}
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}

		}
		return null;

	}

	/**
	 * return the list of FormInputs that match this element
	 *
	 * @param element
	 * @return
	 */
	private FormInput formInputMatchingNode(Node element) {
		NamedNodeMap attributes = element.getAttributes();
		Identification id;

		if (attributes.getNamedItem("id") != null
				&& formFillMode != FormFillMode.XPATH_TRAINING) {
			id = new Identification(Identification.How.id,
					attributes.getNamedItem("id").getNodeValue());
			FormInput input = this.formInputs.get(id);
			if (input != null) {
				return input;
			}
		}

		if (attributes.getNamedItem("name") != null
				&& formFillMode != FormFillMode.XPATH_TRAINING) {
			id = new Identification(Identification.How.name,
					attributes.getNamedItem("name").getNodeValue());
			FormInput input = this.formInputs.get(id);
			if (input != null) {
				return input;
			}
		}

		String xpathExpr = XPathHelper.getXPathExpression(element);
		if (xpathExpr != null && !xpathExpr.equals("")) {
			id = new Identification(Identification.How.xpath, xpathExpr);
			FormInput input = this.formInputs.get(id);
			if (input != null) {
				return input;
			}
		}

		return null;
	}

	private InputType getFormInputType(Node node) {
		String typeSt = getElementType(node);
		return FormInput.getTypeFromStr(typeSt);
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

	/**
	 * Serializes form inputs and writes the data to the output directory to be used by future
	 * non-training crawls.
	 *
	 * @param dir The output directory for the form input data.
	 */
	public static void serializeFormInputs(File dir) {

		if (instance == null) {
			LOGGER.error("No instance of FormInputValueHelper exists.");
			return;
		}

		final File out = new File(dir, FORMS_JSON_FILE);
		Gson json = new GsonBuilder().setPrettyPrinting().create();
		String serialized = json.toJson(instance.formInputs.values());

		try {
			LOGGER.info("Writing training form inputs to " + out.toString());
			FileUtils.writeStringToFile(out, serialized, Charset.defaultCharset());
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

	}

	/**
	 * Serializes form inputs and writes the data to the output directory to be used by future
	 * non-training crawls.
	 *
	 * @param dir The output directory for the form input data.
	 * @return The list of inputs
	 */
	public static List<FormInput> deserializeFormInputs(File dir) {

		List<FormInput> deserialized = new ArrayList<>();
		final File in = new File(dir, FORMS_JSON_FILE);

		if (in.exists()) {
			LOGGER.info("Reading trained form inputs from " + in.getAbsolutePath());
			Gson gson = new GsonBuilder().create();

			try {
				deserialized =
						gson.fromJson(FileUtils.readFileToString(in, Charset.defaultCharset()),
								new TypeToken<List<FormInput>>() {
								}.getType());

			} catch (JsonSyntaxException | IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}

		return deserialized;

	}

}
