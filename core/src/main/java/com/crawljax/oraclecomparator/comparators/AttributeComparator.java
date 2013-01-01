package com.crawljax.oraclecomparator.comparators;

import java.util.ArrayList;
import java.util.List;

import com.crawljax.oraclecomparator.AbstractComparator;

/**
 * Oracle Comparator that ignores the specified attributes.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
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

	private String stripAttributes(String dom) {
		for (String attribute : ignoreAttributes) {
			String regExp = "\\s" + attribute + "=\"[^\"]*\"";
			dom = dom.replaceAll(regExp, "");
		}
		return dom;
	}

	@Override
	public boolean isEquivalent() {
		setOriginalDom(stripAttributes(getOriginalDom()));
		setNewDom(stripAttributes(getNewDom()));
		return super.compare();
	}
}
