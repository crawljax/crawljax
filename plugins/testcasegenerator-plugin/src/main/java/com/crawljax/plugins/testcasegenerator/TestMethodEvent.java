package com.crawljax.plugins.testcasegenerator;

import java.util.List;
import java.util.Map;

public class TestMethodEvent {
	private Map<String, String> properties;
	private List<Map<String, String>> formInputs;

	public TestMethodEvent() {

	}

	public TestMethodEvent(Map<String, String> properties,
	        List<Map<String, String>> formInputs) {
		super();
		this.properties = properties;
		this.formInputs = formInputs;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public List<Map<String, String>> getFormInputs() {
		return formInputs;
	}

	public void setFormInputs(List<Map<String, String>> formInputs) {
		this.formInputs = formInputs;
	}

}
