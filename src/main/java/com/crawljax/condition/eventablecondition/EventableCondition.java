package com.crawljax.condition.eventablecondition;

import java.util.ArrayList;
import java.util.List;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.Condition;
import com.crawljax.util.XPathHelper;

/**
 * An EventableCondition specifies properties of an eventable which should be satisfied in order to
 * crawl the element. The user does not need this class when using the API. Only for use with
 * properties files .
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
public class EventableCondition {

	private final String id;
	private List<Condition> conditions = new ArrayList<Condition>();
	private String inXPath;
	private List<String> linkedInputFields = new ArrayList<String>();

	/**
	 * @param id
	 *            Identifier.
	 */
	public EventableCondition(String id) {
		this.id = id;
	}

	/**
	 * @param id
	 *            Identifier.
	 * @param linkedInputFields
	 *            List of input fields.
	 */
	public EventableCondition(String id, List<String> linkedInputFields) {
		this.id = id;
		this.linkedInputFields = linkedInputFields;
	}

	/**
	 * @param id
	 *            Identifier.
	 * @param conditions
	 *            Conditions that should be satisfied.
	 */
	public EventableCondition(String id, Condition... conditions) {
		this.id = id;
		for (Condition condition : conditions) {
			this.conditions.add(condition);
		}
	}

	@Override
	public String toString() {
		String ret = "id: " + id;
		ret += " xpath: " + getInXPath();
		ret += " linked inputs: " + getLinkedInputFields().toString();
		ret += " conditions: " + conditions.toString();
		return ret;
	}

	/**
	 * @param browser
	 *            The browser.
	 * @return true iff all the conditions are satisfied.
	 */
	public boolean checkAllConditionsSatisfied(EmbeddedBrowser browser) {
		for (Condition condition : getConditions()) {
			if (!condition.check(browser)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return The conditions.
	 */
	public List<Condition> getConditions() {
		return conditions;
	}

	/**
	 * @return The inXPath.
	 */
	public String getInXPath() {
		return inXPath;
	}

	/**
	 * @return The linked input fields.
	 */
	public List<String> getLinkedInputFields() {
		return linkedInputFields;
	}

	/**
	 * @param linkedInputFields
	 *            The linked input fields.
	 */
	public void setLinkedInputFields(List<String> linkedInputFields) {
		this.linkedInputFields = linkedInputFields;
	}

	/**
	 * @param inXPath
	 *            The inXPath expression.
	 */
	public void setInXPath(String inXPath) {
		this.inXPath = XPathHelper.formatXPath(inXPath);
	}

	/**
	 * @param conditions
	 *            the conditions to set
	 */
	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

	/**
	 * Add a new condition to the list of conditions.
	 * 
	 * @param condition
	 *            The condition.
	 */
	public void addCondition(Condition condition) {
		this.conditions.add(condition);
	}

}
