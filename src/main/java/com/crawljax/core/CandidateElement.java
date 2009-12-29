package com.crawljax.core;

import java.util.ArrayList;
import java.util.List;


import org.w3c.dom.Element;

import com.crawljax.condition.eventablecondition.EventableCondition;
import com.crawljax.forms.FormInput;
import com.crawljax.util.Helper;

/**
 * Candidate element for crawling.
 * It is possible to link this eventable to form inputs, so that crawljax knows which values
 * to set for this elements before it is clicked.
 * 
 * @author Danny Roest (dannyroest@gmail.com)
 * @version $Id: CandidateElement.java 6234 2009-12-18 13:46:37Z mesbah $
 */
public class CandidateElement {

	private final Element element;
	private final String xpath;
	private List<FormInput> formInputs = new ArrayList<FormInput>();
	private EventableCondition eventableCondition;

	/**
	 * @param element the element
	 * @param xpath the xpath expression of the element
	 */
	public CandidateElement(Element element, String xpath) {
		super();
		this.element = element;
		this.xpath = xpath;
	}

	/**
	 * @return unique string without atusa attribute
	 */
	public String getGeneralString() {
		List<String> exclude = new ArrayList<String>();
		exclude.add("atusa");
		return this.element.getNodeName() + ": "
		                + Helper.getElementAttributes(this.element, exclude) + " " + this.xpath;
	}

	/**
	 * @return unique string of this candidate element
	 */
	public String getUniqueString() {
		return this.element.getNodeName() + ": " + Helper.getAllElementAttributes(this.element)
		                + " " + this.xpath;
	}

	/**
	 * @return the element
	 */
	public Element getElement() {
		return element;
	}

	/**
	 * @return the xpath of the element
	 */
	public String getXpath() {
		return xpath;
	}

	/**
	 * @return list with related formInputs
	 */
	public List<FormInput> getFormInputs() {
		return formInputs;
	}

	/**
	 * @param formInputs set the related formInputs
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
	 * @param eventableCondition the EventableCondition
	 */
	public void setEventableCondition(EventableCondition eventableCondition) {
		this.eventableCondition = eventableCondition;
	}

}
