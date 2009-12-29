/**
 * 
 */
package com.crawljax.oracle.oracles;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.crawljax.oracle.AbstractOracle;
import com.crawljax.util.Helper;

/**
 * Oracle which can ignore element/attributes by xpath expression.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $id$
 */
public class XPathExpressionOracle extends AbstractOracle {

	private static final Logger LOGGER = Logger.getLogger(XPathExpressionOracle.class.getName());

	private final List<String> expressions = new ArrayList<String>();

	/**
	 * @param expressions
	 *            the xpath expressions to ignore
	 */
	public XPathExpressionOracle(List<String> expressions) {
		this.expressions.addAll(expressions);
	}

	/**
	 * @param expressions
	 *            the xpath expressions to ignore
	 */
	public XPathExpressionOracle(String... expressions) {
		for (String expression : expressions) {
			this.expressions.add(expression);
		}
	}

	/**
	 * 
	 */
	public XPathExpressionOracle() {
	}

	/**
	 * @param originalDom
	 *            The original DOM.
	 * @param newDom
	 *            The new DOM.
	 */
	public XPathExpressionOracle(String originalDom, String newDom) {
		super(originalDom, newDom);
	}

	/**
	 * Add another expression.
	 * 
	 * @param expression
	 *            The expression.
	 */
	public void addExpression(String expression) {
		expressions.add(expression);
	}

	/**
	 * @param dom
	 *            the dom to ignore the xpath expressions from
	 * @return the stripped dom with the elements found with the xpath expressions
	 */
	public String stripXPathExpressions(String dom) {
		String curExpression = "";
		try {
			Document doc = Helper.getDocument(dom);
			for (String expression : expressions) {
				curExpression = expression;
				NodeList nodeList = Helper.getElementsByXpath(doc, expression);

				for (int i = 0; i < nodeList.getLength(); i++) {
					Node node = nodeList.item(i);
					if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
						((Attr) node).getOwnerElement().removeAttribute(node.getNodeName());
					} else if (node.getNodeType() == Node.ELEMENT_NODE) {
						Node parent = node.getParentNode();
						parent.removeChild(node);
					}

				}
			}
			dom = Helper.getDocumentToString(doc);
		} catch (Exception e) {
			LOGGER.error("Error with stripping XPath expression: " + curExpression, e);
		}
		return dom;
	}

	/*
	 * (non-Javadoc)
	 * @see nl.tudelft.swerl.util.oracle.OracleAbstract#isEquivalent()
	 */
	@Override
	public boolean isEquivalent() {
		setOriginalDom(stripXPathExpressions(getOriginalDom()));
		setNewDom(stripXPathExpressions(getNewDom()));
		return super.compare();
	}

}
