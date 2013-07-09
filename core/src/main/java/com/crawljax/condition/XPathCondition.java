package com.crawljax.condition;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import net.jcip.annotations.Immutable;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.util.DomUtils;
import com.crawljax.util.XPathHelper;
import com.google.common.base.Objects;

/**
 * A condition which returns true if the XPath expression returns one or more elements. NOTE:
 * element names must be in upper case and attributes in lower case.
 */
@Immutable
public class XPathCondition implements Condition {

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
			Document document = DomUtils.asDocument(browser.getStrippedDom());
			NodeList nodeList = XPathHelper.evaluateXpathExpression(document, expression);
			return nodeList.getLength() > 0;
		} catch (XPathExpressionException | IOException e) {
			// Exception is caught, check failed so return false;
			return false;
		}

	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getClass(), expression);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof XPathCondition) {
			XPathCondition that = (XPathCondition) object;
			return Objects.equal(this.expression, that.expression);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
		        .add("expression", expression)
		        .toString();
	}

}
