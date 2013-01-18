package com.crawljax.util;

import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLElement;

/**
 * Attribute injector class.
 * 
 * @version $Id$
 */
public final class AttributeInjector {

	private static final Logger LOG = LoggerFactory.getLogger(AttributeInjector.class);

	/**
	 * Find the corresponding 'real' DOM element in the browser for element and inject the unique
	 * attribute.
	 * 
	 * @param element
	 *            The element.
	 * @param attrName
	 *            The attribute name.
	 * @param value
	 *            The value.
	 * @return An HTMLElement consisting of the arguments.
	 */
	public static HTMLElement append(HTMLElement element, String attrName, String value) {
		// append
		String srcAttrValue = element.getAttribute("src");
		// no src attribute?
		if (srcAttrValue.equals("")) {
			return null;
		}
		srcAttrValue += "?" + attrName + "=" + value;
		// System.out.println("Setting value for src to: " + srcAttrValue);
		element.setAttribute("src", srcAttrValue);
		return element;
	}

	/**
	 * Return true iff the node contains the injected attribute.
	 * 
	 * @param node
	 *            The node.
	 * @param attrName
	 *            The attribute name.
	 * @param value
	 *            The value of the attribute.
	 * @param checkValue
	 *            Whether to do a real check.
	 * @return Whether it is injected.
	 */
	public static boolean isInjected(Node node, String attrName, String value, boolean checkValue) {

		NamedNodeMap attributes = node.getAttributes();
		// String tmp = "";
		// if there are no attributes we know the node does not contain the
		// injected attribute
		if (attributes == null) {
			return false;
		}

		Node attr = attributes.getNamedItem(attrName);
		if (attr == null) {
			return false;
		}
		if (!checkValue) {
			return true;
		}

		// verify that the attribute has the correct value
		return StringUtils.trim(attr.getNodeValue()).equals(value);

	}

	/**
	 * Remove the injected attributes from the HTML elements stored in injectedElements.
	 * 
	 * @param injectedElements
	 *            The injected elements.
	 * @param attrName
	 *            The attributes to remove from the injected elements.
	 */
	public static void removeInjectedAttributes(Vector<HTMLElement> injectedElements,
	        String attrName) {
		for (HTMLElement e : injectedElements) {
			removeInjectedAttributes(e, attrName);
		}
	}

	/**
	 * Removes attribute from element.
	 * 
	 * @param element
	 *            The element.
	 * @param attrName
	 *            The attribute to remove from element.
	 */
	public static void removeInjectedAttributes(HTMLElement element, String attrName) {
		try {
			element.removeAttribute(attrName);
		} catch (DOMException exc) {
			LOG.warn("Element {} was removed from DOM", element.getId());
		}
	}

	private AttributeInjector() {
	}

}
