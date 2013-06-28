package com.crawljax.web.model;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 2013/06/21
 * Time: 1:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class Parameter {
	protected String id;
	protected String displayName;
	protected String value;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
