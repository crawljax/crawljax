package com.crawljax.core;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import com.crawljax.condition.eventablecondition.EventableCondition;
import com.crawljax.core.state.Identification;
import com.crawljax.forms.FormInput;
import com.crawljax.util.Helper;

/**
 * Candidate element for crawling. It is possible to link this eventable to form inputs, so that
 * crawljax knows which values to set for this elements before it is clicked.
 * 
 * @author Danny Roest (dannyroest@gmail.com)
 * @version $Id$
 */
public class CandidateElement {

	// default is onclick
	private String eventType = "onclick";

	private Identification identification;

	private Element element;

	private List<FormInput> formInputs = new ArrayList<FormInput>();
	private EventableCondition eventableCondition;

	/**
	 * @param eventType
	 *            the event type.
	 * @param element
	 *            the element.
	 * @param identification
	 *            the identification.
	 */
	public CandidateElement(Element element, String eventType, Identification identification) {
		this.eventType = eventType;
		this.identification = identification;
		this.element = element;
	}

	/**
	 * @param element
	 *            the element
	 * @param xpath
	 *            the xpath expression of the element
	 */
	public CandidateElement(Element element, String xpath) {
		this.element = element;
		identification = new Identification("xpath", xpath);
	}

	/**
	 * @return unique string without atusa attribute
	 */
	public String getGeneralString() {
		List<String> exclude = new ArrayList<String>();
		exclude.add("atusa");

		String result = "";
		if (element != null) {
			result += this.element.getNodeName() + ": ";

		}

		result += Helper.getElementAttributes(this.element, exclude) + " " + this.identification;

		return result;
	}

	/**
	 * @return unique string of this candidate element
	 */
	public String getUniqueString() {

		String result = "";

		if (element != null) {
			result +=
			        this.element.getNodeName() + ": "
			                + Helper.getAllElementAttributes(this.element) + " ";
		}

		result += this.identification + " " + eventType;

		return result;
	}

	/**
	 * @return the element
	 */
	public Element getElement() {
		return element;
	}

	/**
	 * @return list with related formInputs
	 */
	public List<FormInput> getFormInputs() {
		return formInputs;
	}

	/**
	 * @param formInputs
	 *            set the related formInputs
	 */
	public void setFormInputs(List<FormInput> formInputs) {
		this.formInputs = formInputs;
	}

	/**
	 * @return its EventableCondition (can be null)
	 */
	public EventableCondition getEventableCondition() {
		return eventableCondition;
	}

	/**
	 * @param eventableCondition
	 *            the EventableCondition
	 */
	public void setEventableCondition(EventableCondition eventableCondition) {
		this.eventableCondition = eventableCondition;
	}

	/**
	 * @return the identification object.
	 */
	public Identification getIdentification() {
		return identification;
	}

	/**
	 * @return the event type.
	 */
	public String getEventType() {
		return eventType;
	}

}
