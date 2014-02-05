package com.crawljax.web.model;

public class Comparator {
	private ComparatorType type;
	private String expression;

	public enum ComparatorType {
		attribute, date, regex, script, distance, simple, plain, style, xpath
	}

	/**
	 * @return the type
	 */
	public ComparatorType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(ComparatorType type) {
		this.type = type;
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
