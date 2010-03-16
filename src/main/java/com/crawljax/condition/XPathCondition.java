package com.crawljax.condition;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.util.Helper;

/**
 * A condition which returns true if the XPath expression returns one or more elements. NOTE:
 * element names must be in upper case and attributes in lower case.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
public class XPathCondition extends AbstractCondition {

	private final String expression;

	/**
	 * Construct xpath condition.
	 * 
	 * @param expression
	 *            The actual xpath expression.
	 */
	public XPathCondition(String expression) {
		this.expression = expression;
	}

	@Override
	public boolean check(EmbeddedBrowser browser) {
		return checkXPathExpression(browser);
	}

	private boolean checkXPathExpression(EmbeddedBrowser browser) {
		try {
			Document document = Helper.getDocument(browser.getDom());
			NodeList nodeList = Helper.getElementsByXpath(document, expression);
			this.setAffectedNodes(nodeList);
			return nodeList.getLength() > 0;
		} catch (Exception e) {
			throw new RuntimeException("Error with " + this.getClass().getSimpleName() + ": "
			        + expression + "\n" + e.toString());
		}
	}

}
