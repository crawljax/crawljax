/**
 * 
 */
package com.crawljax.oraclecomparator.comparators;

import com.crawljax.oraclecomparator.AbstractComparator;

public class PlainStructureComparator extends AbstractComparator {

	private boolean removeAttributes = true;

	/**
	 * Default argument less constructor.
	 */
	public PlainStructureComparator() {
	}

	/**
	 * @param originalDom
	 *            The original DOM.
	 * @param newDom
	 *            The new DOM.
	 */
	public PlainStructureComparator(String originalDom, String newDom) {
		super(originalDom, newDom);
	}

	@Override
	public boolean isEquivalent() {
		strip();
		return super.compare();
	}

	private String stripAttributes(String string) {
		String regExAttributes = "<(.+?)(\\s.*?)?(/)?>";
		string = string.replaceAll(regExAttributes, "<$1$3>");
		return string;
	}

	private String stripContent(String string) {
		// remove linebreaks
		string = string.replaceAll("[\\t\\n\\x0B\\f\\r]", "");

		// remove content
		string = string.replaceAll(">(.*?)<", "><");
		return string;
	}

	private void strip() {
		if (removeAttributes) {
			setOriginalDom(stripAttributes(getOriginalDom()));
			setNewDom(stripAttributes(getNewDom()));
		}

		setOriginalDom(stripContent(getOriginalDom()));
		setNewDom(stripContent(getNewDom()));

	}

	/**
	 * @param removeAttributes
	 *            the removeAttributes to set
	 */
	public void setRemoveAttributes(boolean removeAttributes) {
		this.removeAttributes = removeAttributes;
	}

}
