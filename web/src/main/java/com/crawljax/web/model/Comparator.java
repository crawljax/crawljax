package com.crawljax.web.model;

import java.util.ArrayList;
import java.util.List;

public class Comparator {
	private ComparatorType type;
	private List<String> args = new ArrayList<String>();
	
	public enum ComparatorType { attribute, date, regex, script, distance, simple, plain, style, xpath }

	/**
	 * @return the type
	 */
	public ComparatorType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(ComparatorType type) {
		this.type = type;
	}

	/**
	 * @return the args
	 */
	public List<String> getArgs() {
		return args;
	}

	/**
	 * @param args the args to set
	 */
	public void setArgs(List<String> args) {
		this.args = args;
	}
}
