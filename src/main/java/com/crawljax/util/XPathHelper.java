/**
 * Created Dec 13, 2007
 */
package com.crawljax.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility class that contains methods used by Crawljax and some plugin to deal with XPath
 * resolving, constructing etc.
 * 
 * @author mesbah
 * @version $Id$
 */
public final class XPathHelper {
	/**
     * 
     */
	private static final String FULL_XPATH_CACHE = "FULL_XPATH_CACHE";
	private static final int MAX_SEARCH_LOOPS = 10000;

	private XPathHelper() {
	}

	/**
	 * Reverse Engineers an XPath Expression of a given Node in the DOM.
	 * 
	 * @param node
	 *            the given node.
	 * @return string xpath expression (e.g., "/html[1]/body[1]/div[3]").
	 */
	public static String getXPathExpression(Node node) {
		Object xpathCache = node.getUserData(FULL_XPATH_CACHE);
		if (xpathCache != null) {
			return xpathCache.toString();
		}
		Node parent = node.getParentNode();

		if ((parent == null) || parent.getNodeName().contains("#document")) {
			String xPath = "/" + node.getNodeName() + "[1]";
			node.setUserData(FULL_XPATH_CACHE, xPath, null);
			return xPath;
		}

		StringBuffer buffer = new StringBuffer();

		if (parent != node) {
			buffer.append(getXPathExpression(parent));
			buffer.append("/");
		}

		buffer.append(node.getNodeName());

		List<Node> mySiblings = getSiblings(parent, node);

		for (int i = 0; i < mySiblings.size(); i++) {
			Node el = mySiblings.get(i);

			if (el.equals(node)) {
				buffer.append("[");
				buffer.append(Integer.toString(i + 1));
				buffer.append("]");
				// Found so break;
				break;
			}
		}
		String xPath = buffer.toString();
		node.setUserData(FULL_XPATH_CACHE, xPath, null);
		return xPath;
	}

	/**
	 * Get siblings of the same type as element from parent.
	 * 
	 * @param parent
	 *            parent node.
	 * @param element
	 *            element.
	 * @return List of sibling (from element) under parent
	 */
	public static List<Node> getSiblings(Node parent, Node element) {
		List<Node> result = new ArrayList<Node>();
		NodeList list = parent.getChildNodes();

		for (int i = 0; i < list.getLength(); i++) {
			Node el = list.item(i);

			if (el.getNodeName().equals(element.getNodeName())) {
				result.add(el);
			}
		}

		return result;
	}

	/**
	 * Returns the list of nodes which match the expression xpathExpr in the String domStr.
	 * 
	 * @param domStr
	 *            the string of the document to search in
	 * @param xpathExpr
	 *            the xpath query
	 * @author cor-paul
	 * @return the list of nodes which match the query
	 * @throws Exception
	 *             On erorr.
	 */
	public static NodeList evaluateXpathExpression(String domStr, String xpathExpr)
	        throws Exception {
		Document dom = Helper.getDocument(domStr);

		return evaluateXpathExpression(dom, xpathExpr);
	}

	/**
	 * Returns the list of nodes which match the expression xpathExpr in the Document dom.
	 * 
	 * @param dom
	 *            the Document to search in
	 * @param xpathExpr
	 *            the xpath query
	 * @author cor-paul
	 * @return the list of nodes which match the query
	 * @throws XPathExpressionException
	 *             On error.
	 */
	public static NodeList evaluateXpathExpression(Document dom, String xpathExpr)
	        throws XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression expr = xpath.compile(xpathExpr);
		Object result = expr.evaluate(dom, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;

		return nodes;
	}

	/**
	 * Returns the XPaths of all nodes retrieved by xpathExpression. Example: //DIV[@id='foo']
	 * returns /HTM[1]/BODY[1]/DIV[2]
	 * 
	 * @param dom
	 *            The dom.
	 * @param xpathExpression
	 *            The expression to find the element.
	 * @return list of XPaths retrieved by xpathExpression.
	 */
	public static List<String> getXpathForXPathExpressions(Document dom, String xpathExpression) {
		NodeList nodeList;
		try {
			nodeList = XPathHelper.evaluateXpathExpression(dom, xpathExpression);
		} catch (XPathExpressionException e) {
			return null;
		}
		List<String> result = new ArrayList<String>();
		if (nodeList.getLength() > 0) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node n = nodeList.item(i);
				result.add(getXPathExpression(n));
			}
		}
		return result;
	}

	/**
	 * @param xpath
	 *            The xpath to format.
	 * @return formatted xpath with tag names in uppercase and attributes in lowercase
	 */
	public static String formatXPath(String xpath) {
		String formatted = xpath;
		Pattern p = Pattern.compile("(/(?:[-a-zA-Z]+::)?+)([a-zA-Z]+)");
		Matcher m = p.matcher(formatted);

		for (int i = 0; m.find(i); i++) {
			i = m.start();
			formatted = m.replaceFirst(m.group(1) + m.group(2).toUpperCase());
			m = p.matcher(formatted);
		}
		p = Pattern.compile("(@[a-zA-Z]+)");
		m = p.matcher(formatted);

		for (int i = 0; m.find(i); i++) {
			i = m.start();
			formatted = m.replaceFirst(m.group().toLowerCase());
			m = p.matcher(formatted);
		}
		return formatted;
	}

	/**
	 * @param xpath
	 *            The xpath expression to find the last element of.
	 * @return returns the last element in the xpath expression
	 */
	public static String getLastElementXPath(String xpath) {
		String[] elements = xpath.split("/");
		for (int i = elements.length - 1; i >= 0; i--) {
			if (!elements[i].equals("") && elements[i].indexOf("()") == -1
			        && !elements[i].startsWith("@")) {
				return stripEndSquareBrackets(elements[i]);
			}
		}
		return "";
	}

	/**
	 * @param string
	 * @return string without the before [
	 */
	private static String stripEndSquareBrackets(String string) {
		if (string.indexOf("[") == -1) {
			return string;
		} else {
			return string.substring(0, string.indexOf("["));
		}
	}

	/**
	 * returns position of xpath element which match the expression xpath in the String dom.
	 * 
	 * @param dom
	 *            the Document to search in
	 * @param xpath
	 *            the xpath query
	 * @author Danny
	 * @return position of xpath element, if fails returns -1
	 **/
	public static int getXPathLocation(String dom, String xpath) {
		dom = dom.toLowerCase();
		xpath = xpath.toLowerCase();
		String[] elements = xpath.split("/");
		int pos = 0;
		int temp;
		int number;
		// System.out.println(xpath);
		for (String element : elements) {
			if (!element.equals("") && !element.startsWith("@") && element.indexOf("()") == -1) {
				if (element.indexOf("[") != -1) {
					try {
						number =
						        Integer.parseInt(element.substring(element.indexOf("[") + 1,
						                element.indexOf("]")));
					} catch (Exception e) {
						return -1;
					}
				} else {
					number = 1;
				}
				// System.out.println("number: " + number);
				for (int i = 0; i < number; i++) {
					// find new open element
					temp = dom.indexOf("<" + stripEndSquareBrackets(element), pos);

					if (temp > -1) {
						pos = temp + 1;

						// if depth>1 then goto end of current element
						if (number > 1 && i < number - 1) {
							pos =
							        getCloseElementLocation(dom, pos,
							                stripEndSquareBrackets(element));
							// pos = dom.indexOf("<" +
							// stripEndSquareBrackets(element), pos) + 1;
						}

					}
				}
			}
		}
		return pos - 1;
	}

	/**
	 * @param dom
	 *            The dom string.
	 * @param pos
	 *            Position where to start searching.
	 * @param element
	 *            The element.
	 * @return the position where the close element is
	 */
	public static int getCloseElementLocation(String dom, int pos, String element) {
		String[] elements = { "LINK", "META", "INPUT", "BR" };
		List<String> singleElements = Arrays.asList(elements);
		if (singleElements.contains(element.toUpperCase())) {
			return dom.indexOf(">", pos) + 1;
		}
		// make sure not before the node
		int openElements = 1;
		int i = 0;
		dom = dom.toLowerCase();
		element = element.toLowerCase();
		String openElement = "<" + element;
		String closeElement = "</" + element;
		while (i < MAX_SEARCH_LOOPS) {
			if (dom.indexOf(openElement, pos) == -1 && dom.indexOf(closeElement, pos) == -1) {
				return -1;
			}
			// System.out.println("hierzo: " + dom.substring(pos));
			if (dom.indexOf(openElement, pos) < dom.indexOf(closeElement, pos)
			        && dom.indexOf(openElement, pos) != -1) {
				openElements++;
				pos = dom.indexOf(openElement, pos) + 1;
				// System.out.println("open: " + dom.substring(pos-1));
			} else {

				openElements--;
				pos = dom.indexOf(closeElement, pos) + 1;
				// System.out.println("close: " + dom.substring(pos-1));
			}
			// System.out.println(openElements);
			if (openElements == 0) {
				break;
			}
			i++;
		}
		// System.out.println("Finished: " + dom.substring(pos-1));
		return pos - 1;

	}

	/**
	 * @param dom
	 *            The dom.
	 * @param xpath
	 *            The xpath expression.
	 * @return the position where the close element is
	 */
	public static int getCloseElementLocation(String dom, String xpath) {
		return getCloseElementLocation(dom, getXPathLocation(dom, xpath) + 1,
		        getLastElementXPath(xpath));
	}

	/**
	 * @param xpath
	 *            The xpath expression.
	 * @return the xpath expression for only the element location. Leaves out the attributes and
	 *         text()
	 */
	public static String stripXPathToElement(String xpath) {
		if (xpath != null && !xpath.equals("")) {
			if (xpath.toLowerCase().indexOf("/text()") != -1) {
				xpath = xpath.substring(0, xpath.toLowerCase().indexOf("/text()"));
			}
			if (xpath.toLowerCase().indexOf("/comment()") != -1) {
				xpath = xpath.substring(0, xpath.toLowerCase().indexOf("/comment()"));
			}
			if (xpath.indexOf("@") != -1) {
				xpath = xpath.substring(0, xpath.indexOf("@") - 1);
			}
		}

		return xpath;
	}

}
