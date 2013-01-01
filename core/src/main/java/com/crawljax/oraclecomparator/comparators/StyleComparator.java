package com.crawljax.oraclecomparator.comparators;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.crawljax.oraclecomparator.AbstractComparator;
import com.crawljax.util.Helper;
import com.crawljax.util.XPathHelper;

/**
 * Oracle which can ignore style attributes.
 * 
 * @version $Id$
 */
public class StyleComparator extends AbstractComparator {

	private static final Logger LOGGER = LoggerFactory.getLogger(StyleComparator.class.getName());

	private static final String[] IGNORE_ATTRIBUTES = { "align", "bgcolor", "height", "valign",
	        "width", "type", "dir" };
	private static final String[] IGNORE_TAGS = { "em", "strong", "dfn", "code", "samp", "kdb",
	        "var", "cite", "tt", "b", "i", "u", "big", "small", "pre", "font" };
	private static final String[] ALLOW_STYLE_TYPES = { "display", "visibility" };

	/**
	 * Default argument less constructor.
	 */
	public StyleComparator() {
		super();
	}

	/**
	 * @param originalDom
	 *            The original DOM.
	 * @param newDom
	 *            The new DOM.
	 */
	public StyleComparator(String originalDom, String newDom) {
		super(originalDom, newDom);
	}

	@Override
	public boolean isEquivalent() {
		try {
			setOriginalDom(Helper.getDocumentToString(stripDom(Helper
			        .getDocument(getOriginalDom()))));
			setNewDom(Helper.getDocumentToString(stripDom(Helper.getDocument(getNewDom()))));
		} catch (SAXException e) {
			LOGGER.error(e.getMessage(), e);
			return false;
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			return false;
		}
		return super.compare();
	}

	private Document stripDom(Document dom) {
		dom = stripElements(dom);
		dom = stripAttributes(dom);
		dom = stripStyleAttributes(dom);
		return dom;
	}

	private Document stripStyleAttributes(Document dom) {
		try {
			NodeList nl = XPathHelper.evaluateXpathExpression(dom, "//*[@style]/@style");
			for (int i = 0; i < nl.getLength(); i++) {
				Node attribute = nl.item(i);
				if (attribute != null) {
					attribute.setNodeValue(stripStyleProperties(attribute.getNodeValue()));
					if (attribute.getNodeValue() == "") {
						((Attr) attribute).getOwnerElement().removeAttribute(
						        attribute.getNodeName());
					}
				}
			}
		} catch (XPathExpressionException e) {
			LOGGER.warn("Error with StyleOracle: " + e.getMessage());
			LOGGER.error(e.getMessage(), e);
		} catch (DOMException e) {
			LOGGER.warn("Error with StyleOracle: " + e.getMessage());
			LOGGER.error(e.getMessage(), e);
		}
		return dom;
	}

	private Document stripElements(Document dom) {
		for (String tag : IGNORE_TAGS) {
			try {
				NodeList nl = XPathHelper.evaluateXpathExpression(dom, "//" + tag.toUpperCase());
				for (int i = 0; i < nl.getLength(); i++) {
					Node removeNode = nl.item(i);
					Node parent = removeNode.getParentNode();
					Node nextSibling = removeNode.getNextSibling();

					NodeList children = removeNode.getChildNodes();
					if (children != null && children.getLength() > 0) {
						if (nextSibling == null) {
							parent.appendChild(children.item(0));
						} else {
							parent.insertBefore(children.item(0), nextSibling);
						}
					}
					parent.removeChild(removeNode);
				}
			} catch (XPathExpressionException e) {
				LOGGER.warn("Error with StyleOracle: " + e.getMessage());
				LOGGER.error(e.getMessage(), e);
			} catch (DOMException e) {
				LOGGER.warn("Error with StyleOracle: " + e.getMessage());
				LOGGER.error(e.getMessage(), e);
			}
		}
		return dom;
	}

	private Document stripAttributes(Document dom) {
		for (String attributeName : IGNORE_ATTRIBUTES) {
			String attribute = attributeName.toLowerCase();
			try {
				NodeList nl = XPathHelper.evaluateXpathExpression(dom, "//*[@" + attribute + "]");
				for (int i = 0; i < nl.getLength(); i++) {
					NamedNodeMap attributes = nl.item(i).getAttributes();
					attributes.removeNamedItem(attribute);
				}
			} catch (XPathExpressionException e) {
				LOGGER.warn("Error with StyleOracle: " + e.getMessage());
			} catch (DOMException e) {
				LOGGER.warn("Error with StyleOracle: " + e.getMessage());
			}
		}
		return dom;
	}

	private String stripStyleProperties(String styleAttribute) {
		String[] styleProperties = styleAttribute.split(";");
		String[] styleProperty;
		String badWayOfDoingThis = "";
		StringBuffer buffer = new StringBuffer();

		for (int i = 0; i < styleProperties.length; i++) {
			styleProperty = styleProperties[i].split(":");
			if (styleProperty.length == 2) {
				for (int j = 0; j < ALLOW_STYLE_TYPES.length; j++) {
					if (styleProperty[0].trim().equalsIgnoreCase(ALLOW_STYLE_TYPES[j])) {
						badWayOfDoingThis +=
						        styleProperty[0].trim() + ": " + styleProperty[1].trim() + ";";
						buffer.append(styleProperty[0].trim());
						buffer.append(": ");
						buffer.append(styleProperty[1].trim());
						buffer.append(";");
					}
				}
			}
		}
		// TODO This should be buffer.toString() but a weird behavior of this class results in a
		// difference.
		// return buffer.toString();
		return badWayOfDoingThis;
	}

}
