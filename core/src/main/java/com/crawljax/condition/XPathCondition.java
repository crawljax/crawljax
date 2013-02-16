package com.crawljax.condition;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import net.jcip.annotations.Immutable;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.util.DomUtils;
import com.crawljax.util.XPathHelper;

/**
 * A condition which returns true if the XPath expression returns one or more elements. NOTE:
 * element names must be in upper case and attributes in lower case.
 */
@Immutable
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
			Document document = DomUtils.asDocument(browser.getDom());
			NodeList nodeList = XPathHelper.evaluateXpathExpression(document, expression);
			// TODO IF this can be removed, the ThreadLocal store can be removed and increasing
			// speed!
			this.setAffectedNodes(nodeList);
			return nodeList.getLength() > 0;
		} catch (XPathExpressionException | IOException e) {
			// Exception is catched, check failed so return false;
			return false;
		}

	}

}
