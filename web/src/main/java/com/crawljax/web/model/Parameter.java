package com.crawljax.web.model;

import java.util.ArrayList;
import java.util.List;

public class Parameter {
	public enum ParameterType {
		textbox,
		checkbox,
		select
	}

	private String id;
	private String displayName;
	private ParameterType type;
	private List<SelectOption> options = new ArrayList<>();
	private String value;

	public Parameter() {}

	public Parameter(Parameter parameter) {
		this.id = parameter.getId();
		this.displayName = parameter.getDisplayName();
		this.type = parameter.getType();
		for(int i = 0; i < parameter.getOptions().size(); i++) {
			this.options.add(new SelectOption(parameter.getOptions().get(i)));
		}
		this.value = parameter.getValue();
	}

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

	public ParameterType getType() {
		return type;
	}

	public void setType(ParameterType type) {
		this.type = type;
	}

	public List<SelectOption> getOptions() {
		return options;
	}

	public void setOptions(List<SelectOption> options) {
		this.options = options;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
