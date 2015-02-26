package com.crawljax.oraclecomparator.comparators;

import java.util.ArrayList;
import java.util.List;

import com.crawljax.oraclecomparator.AbstractComparator;

/**
 * Oracle Comparator that ignores the specified attributes.
 */
public class AttributeComparator extends AbstractComparator {

	private final List<String> ignoreAttributes = new ArrayList<String>();

	/**
	 * @param attributes
	 *            the attributes to ignore
	 */
	public AttributeComparator(String... attributes) {
		for (String attribute : attributes) {
			ignoreAttributes.add(attribute);
		}
	}

	@Override
	public String normalize(String dom) {
		String strippedDom = dom;
		for (String attribute : ignoreAttributes) {
			String regExp = "\\s" + attribute + "=\"[^\"]*\"";
			strippedDom = strippedDom.replaceAll(regExp, "");
		}
		return strippedDom;
	}
}
