package com.crawljax.oraclecomparator.comparators;

import com.crawljax.oraclecomparator.AbstractComparator;
import com.crawljax.util.DomUtils;
import com.crawljax.util.XPathHelper;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.Collection;

/**
 * Oracle which can ignore element/attributes by xpath expression.
 */
public class XPathExpressionComparator extends AbstractComparator {

	private static final Logger LOGGER = LoggerFactory.getLogger(XPathExpressionComparator.class
			.getName());

	private final ImmutableList<String> expressions;

	/**
	 * @param expressions the xpath expressions to ignore
	 */
	public XPathExpressionComparator(Collection<String> expressions) {
		this.expressions = ImmutableList.copyOf(expressions);
	}

	/**
	 * @param expressions the xpath expressions to ignore
	 */
	public XPathExpressionComparator(String... expressions) {
		this.expressions = ImmutableList.copyOf(expressions);
	}

	/**
	 * @param dom the dom to ignore the xpath expressions from
	 * @return the stripped dom with the elements found with the xpath expressions
	 */
	@Override
	public String normalize(String dom) {
		String curExpression = "";
		Document doc = null;
		String domRet;
		try {
			doc = DomUtils.asDocument(dom);
			for (String expression : expressions) {
				curExpression = expression;
				NodeList nodeList = XPathHelper.evaluateXpathExpression(doc, expression);

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
		} catch (XPathExpressionException | DOMException | IOException e) {
			LOGGER.error("Exception with stripping XPath expression: " + curExpression, e);
		} finally {
			if (doc != null) {
				domRet = DomUtils.getDocumentToString(doc);
			} else {
				domRet = "";
			}
		}
		return domRet;
	}

}
