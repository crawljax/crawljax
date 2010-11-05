package com.crawljax.util;

import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLElement;

/**
 * Attribute injector class.
 * 
 * @version $Id$
 */
public final class AttributeInjector {

	private AttributeInjector() {

	}

	/**
	 * Inject a unique attribute into element.
	 * 
	 * @param element
	 * @param isActive
	 */
	/*
	 * public static HTMLElement injectUniqueAttributeInNode(
	 * com.jniwrapper.win32.ie.dom.HTMLElement element, String attrName, String value, boolean
	 * injectParents) { if (element == null) return null; element.setAttribute(attrName, value); if
	 * (injectParents) return injectUniqueAttributeInNode(element.getOffsetParent(), attrName,
	 * value, injectParents); return element; }
	 */

	/*
	 * public static HTMLElement injectUniqueAttributeInNode(watij.elements.HtmlElement element,
	 * String attrName, String value, boolean injectParents) { if (element == null) return null; try
	 * { // use reflection to get the 'real' DOM element HTMLElement realElement =
	 * getInternalBrowserElement(element); return injectUniqueAttributeInNode(realElement, attrName,
	 * value, injectParents); } catch (Exception e) { e.printStackTrace(); } return null; }
	 */

	/*
	 * public static HTMLElement getInternalBrowserElement(watij.elements.HtmlElement element)
	 * throws Exception { IEHtmlElement ieHtml = (IEHtmlElement) element; Method ieMethod =
	 * ieHtml.getClass().getDeclaredMethod("htmlElement"); ieMethod.setAccessible(true); HTMLElement
	 * HTMLe = (HTMLElement) ieMethod.invoke(ieHtml); return HTMLe; }
	 */
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

	/*
	 * public static HTMLElement append(watij.elements.HtmlElement element, String attrName, String
	 * value) { try { HTMLElement realElement = getInternalBrowserElement(element); return
	 * append(realElement, attrName, value); } catch (Exception e) { e.printStackTrace(); } return
	 * null; }
	 */

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
			// check if the attr was appended to the src value
			// String srcAttrValue = element.getAttribute("src");
			//			
			// if(srcAttrValue.matches(".*" + attrName + "=.*"))
			// {
			// int index = srcAttrValue.indexOf(attrName);
			// srcAttrValue = srcAttrValue.substring(0, index-1);
			// element.setAttribute("src", srcAttrValue);
			// }

		} catch (Exception exc) {
			System.out.println("Element was removed from DOM");
		}
	}
}
