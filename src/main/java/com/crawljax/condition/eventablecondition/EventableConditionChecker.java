package com.crawljax.condition.eventablecondition;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import com.crawljax.util.XPathHelper;

/**
 * TODO: Document this, what is this class for?
 * 
 * @author mesbah
 * @version $Id$
 */
public class EventableConditionChecker {

	private List<EventableCondition> eventableConditions = new ArrayList<EventableCondition>();

	/**
	 * @return the eventableConditions
	 */
	public List<EventableCondition> getEventableConditions() {
		return eventableConditions;
	}

	/**
	 * @param eventableConditions
	 *            the eventableConditions to set
	 */
	public void setEventableConditions(List<EventableCondition> eventableConditions) {
		this.eventableConditions = eventableConditions;
	}

	/**
	 * @param eventableCondition
	 *            the eventableCondition to add
	 */
	public void addEventableCondition(EventableCondition eventableCondition) {
		this.eventableConditions.add(eventableCondition);
	}

	/**
	 * @param eventableConditions
	 *            The eventable conditions that should be added.
	 */
	public void addEventableConditions(List<EventableCondition> eventableConditions) {
		for (EventableCondition eventableCondition : eventableConditions) {
			this.eventableConditions.add(eventableCondition);
		}
	}

	/**
	 * @param id
	 *            Identifier of the eventablecondition.
	 * @return EventableCondition
	 */
	public EventableCondition getEventableCondition(String id) {
		if (eventableConditions != null && id != null && !id.equals("")) {
			for (EventableCondition eventableCondition : eventableConditions) {
				if (eventableCondition.getId().equalsIgnoreCase(id)) {
					return eventableCondition;
				}
			}
		}

		return null;
	}

	/**
	 * Checks whether an XPath expression starts with an XPath eventable condition.
	 * 
	 * @param dom
	 *            The DOM String.
	 * @param eventableCondition
	 *            The eventable condition.
	 * @param xpath
	 *            The XPath.
	 * @return boolean whether xpath starts with xpath location of eventable condition xpath
	 *         condition
	 * @throws Exception
	 *             when not can be determined whether xpath contains needed xpath locaton
	 */
	public boolean checkXpathStartsWithXpathEventableCondition(Document dom,
	        EventableCondition eventableCondition, String xpath) throws Exception {
		if (eventableCondition == null || eventableCondition.getInXPath() == null
		        || eventableCondition.getInXPath().equals("")) {
			throw new Exception("Eventable has no XPath condition");
		}
		List<String> expressions =
		        XPathHelper.getXpathForXPathExpressions(dom, eventableCondition.getInXPath());

		/* check all expressions */
		for (String fullXpath : expressions) {
			if (xpath.startsWith(fullXpath)) {
				return true;
			}
		}

		return false;
	}

}
