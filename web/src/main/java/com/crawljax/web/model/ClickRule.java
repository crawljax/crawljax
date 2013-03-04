package com.crawljax.web.model;

import java.util.List;

public class ClickRule {
	private RuleType rule = RuleType.click;
	private String elementTag;
	private List<Condition> conditions;

	public enum RuleType {
		click, noClick
	}

	/**
	 * @return the rule
	 */
	public RuleType getRule() {
		return rule;
	}

	/**
	 * @param rule
	 *            the rule to set
	 */
	public void setRule(RuleType rule) {
		this.rule = rule;
	}

	/**
	 * @return the elementTag
	 */
	public String getElementTag() {
		return elementTag;
	}

	/**
	 * @param elementTag
	 *            the elementTag to set
	 */
	public void setElementTag(String elementTag) {
		this.elementTag = elementTag;
	}

	/**
	 * @return the conditions
	 */
	public List<Condition> getConditions() {
		return conditions;
	}

	/**
	 * @param conditions
	 *            the conditions to set
	 */
	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}
}
