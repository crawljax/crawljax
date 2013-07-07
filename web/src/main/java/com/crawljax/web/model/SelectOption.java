package com.crawljax.web.model;

public class SelectOption {
	private String name;
	private String value;

	public SelectOption() {}

	public SelectOption(SelectOption selectOption) {
		this.name = selectOption.getName();
		this.value = selectOption.getValue();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
