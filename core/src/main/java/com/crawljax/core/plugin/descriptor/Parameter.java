package com.crawljax.core.plugin.descriptor;

import com.crawljax.core.plugin.descriptor.jaxb.generated.OptionList;

import java.util.HashMap;
import java.util.Map;

public class Parameter {
	public enum ParameterType {
		textbox,
		checkbox,
		select
	}

	private String id;
	private String displayName;
	private ParameterType type;
	private Map<String, String> options = new HashMap<>();
	private String value;

	public Parameter() {}

	public static Parameter fromJaxbParameter(com.crawljax.core.plugin.descriptor.jaxb.generated.Parameter parameter) {
		Parameter copy = new Parameter();
		copy.id = parameter.getId();
		copy.displayName = parameter.getDisplayName();
		copy.type = Parameter.ParameterType.valueOf(parameter.getType());
		if(parameter.getOptions() != null) {
			for(OptionList.Option option : parameter.getOptions().getOption()) {
				copy.getOptions().put(option.getName(), option.getValue());
			}
		}
		return copy;
	}

	/*public Parameter(Parameter parameter) { //Copy constructor
		this.id = parameter.getId();
		this.displayName = parameter.getDisplayName();
		this.type = parameter.getType();
		for(Map.Entry<String, String> entry : parameter.getOptions().entrySet()) {
			this.options.put(entry.getKey(), entry.getValue());
		}
		this.value = parameter.getValue();
	}*/

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

	public Map<String, String> getOptions() {
		return options;
	}

	public void setOptions(Map<String, String> options) {
		this.options = options;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
