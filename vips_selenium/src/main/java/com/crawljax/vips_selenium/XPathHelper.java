package com.crawljax.vips_selenium;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class XPathHelper {

    private static final String SKEL_XPATH_CACHE = "SKEL_XPATH_CACHE";

    public static String getXPathExpression(Node node) {
        return getSkeletonXpath(node);
    }

    public static String getSkeletonXpath(Node node) {
        if (node == null) {
            return null;
        }

        Object xpathCache = node.getUserData(SKEL_XPATH_CACHE);
        if (xpathCache != null) {
            return xpathCache.toString();
        }
        Node parent = node.getParentNode();

        if ((parent == null) || parent.getNodeName().contains("#document")) {
            String xPath = "/" + node.getNodeName() + "[1]";
            node.setUserData(SKEL_XPATH_CACHE, xPath, null);
            return xPath;
        }

        //		if (node.hasAttributes() && node.getAttributes().getNamedItem("id") != null) {
        //			String xPath = "//" + node.getNodeName() + "[@id = '"
        //					+ node.getAttributes().getNamedItem("id").getNodeValue() + "']";
        //			node.setUserData(FULL_XPATH_CACHE, xPath, null);
        //			return xPath;
        //		}

        StringBuilder buffer = new StringBuilder();

        if (parent != node) {
            buffer.append(getSkeletonXpath(parent));
            buffer.append("/");
        }

        buffer.append(node.getNodeName());

        List<Node> mySiblings = getSiblings(parent, node);

        for (int i = 0; i < mySiblings.size(); i++) {
            Node el = mySiblings.get(i);

            if (el.equals(node)) {
                buffer.append('[').append(Integer.toString(i + 1)).append(']');
                // Found so break;
                break;
            }
        }
        String xPath = buffer.toString();
        node.setUserData(SKEL_XPATH_CACHE, xPath, null);
        return xPath;
    }

    /**
     * Get siblings of the same type as element from parent.
     *
     * @param parent  parent node.
     * @param element element.
     * @return List of sibling (from element) under parent
     */
    public static List<Node> getSiblings(Node parent, Node element) {
        List<Node> result = new ArrayList<>();
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
     * @return the list of nodes which match the query
     * @throws XPathExpressionException
     * @throws IOException
     */
    public static NodeList evaluateXpathExpression(String domStr, String xpathExpr)
            throws XPathExpressionException, IOException {
        Document dom = DomUtils.asDocument(domStr);
        return evaluateXpathExpression(dom, xpathExpr);
    }

    /**
     * Returns the list of nodes which match the expression xpathExpr in the Document dom.
     *
     * @param dom       the Document to search in
     * @param xpathExpr the xpath query
     * @return the list of nodes which match the query
     * @throws XPathExpressionException On error.
     */
    public static NodeList evaluateXpathExpression(Document dom, String xpathExpr) throws XPathExpressionException {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathExpression expr = xpath.compile(xpathExpr);
        Object result = expr.evaluate(dom, XPathConstants.NODESET);
        return (NodeList) result;
    }
}
