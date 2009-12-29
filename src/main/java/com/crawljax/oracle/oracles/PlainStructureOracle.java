/**
 * 
 */
package com.crawljax.oracle.oracles;

import com.crawljax.oracle.AbstractOracle;

/**
 * @author Danny
 */
public class PlainStructureOracle extends AbstractOracle {

	private boolean removeAttributes = true;

	/**
	 * Default argument less constructor.
	 */
	public PlainStructureOracle() {
	}

	/**
	 * @param originalDom
	 *            The original DOM.
	 * @param newDom
	 *            The new DOM.
	 */
	public PlainStructureOracle(String originalDom, String newDom) {
		super(originalDom, newDom);
	}

	/*
	 * (non-Javadoc)
	 * @see nl.tudelft.swerl.util.oracle.OracleAbstract#isEquivalent()
	 */
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

	// public static void main(String[] args) {
	// String dom =
	// "<html>\n\n   <body color='woei'>   <div style='width: 100%'> "
	// + "dahksdhjkasd </div> dahjkdashjkdashjkdas</body></html> ";
	// PlainStructureOracle o = new PlainStructureOracle(dom, dom);
	// o.isEquivalent();
	// System.out.println(o.getOriginalDom());
	// System.out.println(PrettyHTML.prettyHTML(o.getOriginalDom()));
	// }

	/**
	 * @param removeAttributes
	 *            the removeAttributes to set
	 */
	public void setRemoveAttributes(boolean removeAttributes) {
		this.removeAttributes = removeAttributes;
	}

}
