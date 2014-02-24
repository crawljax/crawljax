package com.crawljax.web.model;

public class Condition {
	private ConditionType condition;
	private String expression;

	public enum ConditionType {
		url, notUrl, javascript, regex, notRegex, visibleId, notVisibleId, visibleText,
		notVisibleText, visibleTag, notVisibleTag, xPath, notXPath, wAttribute, wText, wXPath
	}

	/**
	 * @return the condition
	 */
	public ConditionType getCondition() {
		return condition;
	}

	/**
	 * @param condition
	 *            the condition to set
	 */
	public void setCondition(ConditionType condition) {
		this.condition = condition;
	}

	/**
	 * @return the expression
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * @param expression
	 *            the expression to set
	 */
	public void setExpression(String expression) {
		this.expression = expression;
	}
}
