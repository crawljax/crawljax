package com.crawljax.web.model;

import java.util.List;

public class ClickRule {
	private String elementTag;
	private List<NameValuePair> attributes;
	private List<Condition> conditions;
	/**
	 * @return the elementTag
	 */
	public String getElementTag() {
		return elementTag;
	}
	/**
	 * @param elementTag the elementTag to set
	 */
	public void setElementTag(String elementTag) {
		this.elementTag = elementTag;
	}
	/**
	 * @return the attributes
	 */
	public List<NameValuePair> getAttributes() {
		return attributes;
	}
	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(List<NameValuePair> attributes) {
		this.attributes = attributes;
	}
	/**
	 * @return the conditions
	 */
	public List<Condition> getConditions() {
		return conditions;
	}
	/**
	 * @param conditions the conditions to set
	 */
	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}
}
