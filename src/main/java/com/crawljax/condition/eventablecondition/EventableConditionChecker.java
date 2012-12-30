package com.crawljax.condition.eventablecondition;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import com.crawljax.util.XPathHelper;

/**
 * Check whether the conditions of an eventable are satisfied.
 * 
 * @author mesbah
 * @version $Id$
 */
public class EventableConditionChecker {

	private List<EventableCondition> eventableConditions = new ArrayList<EventableCondition>();

	/**
	 * Construct the eventableconditionchecker with its eventable conditions.
	 * 
	 * @param eventableConditions
	 *            The eventable conditions.
	 */
	public EventableConditionChecker(List<EventableCondition> eventableConditions) {
		this.eventableConditions = eventableConditions;
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

		return checkXPathUnderXPaths(xpath, expressions);
	}

	/**
	 * @param xpath
	 *            the xpath to check if its under a certain set of full-xPaths.
	 * @param xpathsList
	 *            the set of full-length-xPaths
	 * @return true if the xpath is under one of the full-length-xpaths.
	 */
	public boolean checkXPathUnderXPaths(String xpath, List<String> xpathsList) {
		/* check all expressions */
		for (String fullXpath : xpathsList) {
			if (xpath.startsWith(fullXpath)) {
				return true;
			}
		}

		return false;
	}

}
