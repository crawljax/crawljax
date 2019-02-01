package com.crawljax.stateabstractions.dom;

import com.crawljax.util.DomUtils;

public class DOMConfiguration {

	public enum Mode {
		ORIGINAL_DOM, STRIPPED_DOM, CONTENT, STRUCTURE
	}

	public static String getConfiguredDOM(String dom, String strippedDOM, Mode mode) {
		switch (mode) {
			case ORIGINAL_DOM:
				return dom;
			case STRIPPED_DOM:
				return strippedDOM;
			case CONTENT:
				return DomUtils.getDOMContent(dom);
			case STRUCTURE:
				return DomUtils.getDOMWithoutContent(dom);
		}

		return "";
	}
}
