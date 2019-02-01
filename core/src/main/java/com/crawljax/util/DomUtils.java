package com.crawljax.util;

import com.crawljax.core.CrawljaxException;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.cyberneko.html.parsers.DOMParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class that contains a number of helper functions used by Crawljax and some plugins.
 */
public final class DomUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomUtils.class
            .getName());

    static final int BASE_LENGTH = 3;

    private static final int TEXT_CUTOFF = 50;

    /**
     * transforms a string into a Document object. TODO This needs more optimizations. As it seems
     * the getDocument is called way too much times causing a lot of parsing which is slow and not
     * necessary.
     *
     * @param html the HTML string.
     * @return The DOM Document version of the HTML string.
     * @throws IOException if an IO failure occurs.
     */
    public static Document asDocument(String html) throws IOException {
        DOMParser domParser = new DOMParser();
        try {
            domParser
                    .setProperty(
                            "http://cyberneko.org/html/properties/names/elems",
                            "match");
            domParser.setFeature("http://xml.org/sax/features/namespaces",
                    false);
            domParser.parse(new InputSource(new StringReader(html)));
        } catch (SAXException e) {
            throw new IOException("Error while reading HTML: " + html, e);
        }
        return domParser.getDocument();
    }

    /**
     * @param html the HTML string.
     * @return a Document object made from the HTML string.
     * @throws SAXException if an exception occurs while parsing the HTML string.
     * @throws IOException  if an IO failure occurs.
     */
    public static Document getDocumentNoBalance(String html)
            throws SAXException, IOException {
        DOMParser domParser = new DOMParser();
        domParser.setProperty(
                "http://cyberneko.org/html/properties/names/elems", "match");
        domParser.setFeature("http://cyberneko.org/html/features/balance-tags",
                false);
        domParser.parse(new InputSource(new StringReader(html)));
        return domParser.getDocument();
    }

    /**
     * @param element The DOM Element.
     * @return A string representation of all the element's attributes.
     */
    public static String getAllElementAttributes(Element element) {
        return getElementAttributes(element, ImmutableSet.of());
    }

    /**
     * @param element The DOM Element.
     * @param exclude the list of exclude strings.
     * @return A string representation of the element's attributes excluding exclude.
     */
    public static String getElementAttributes(Element element,
                                              ImmutableSet<String> exclude) {
        StringBuilder buffer = new StringBuilder();
        if (element != null) {
            NamedNodeMap attributes = element.getAttributes();
            if (attributes != null) {
                addAttributesToString(exclude, buffer, attributes);
            }
        }

        return buffer.toString().trim();
    }

    private static void addAttributesToString(ImmutableSet<String> exclude,
                                              StringBuilder buffer, NamedNodeMap attributes) {
        for (int i = 0; i < attributes.getLength(); i++) {
            Attr attr = (Attr) attributes.item(i);
            if (!exclude.contains(attr.getNodeName())) {
                buffer.append(attr.getNodeName()).append('=');
                buffer.append(attr.getNodeValue()).append(' ');
            }
        }
    }

    /**
     * @param element the element.
     * @return a string representation of the element including its attributes.
     */
    public static String getElementString(Element element) {
        String text = DomUtils.removeNewLines(DomUtils.getTextValue(element))
                .trim();
        StringBuilder info = new StringBuilder();
        if (!Strings.isNullOrEmpty(text)) {
            info.append("\"").append(text).append("\" ");
        }
        if (element != null) {
            if (element.hasAttribute("id")) {
                info.append("ID: ").append(element.getAttribute("id"))
                        .append(" ");
            }
            info.append(DomUtils.getAllElementAttributes(element)).append(" ");
        }
        return info.toString();
    }

    /**
     * @param dom   the DOM document.
     * @param xpath the xpath.
     * @return The element found on DOM having the xpath position.
     * @throws XPathExpressionException if the xpath fails.
     */
    public static Element getElementByXpath(Document dom, String xpath)
            throws XPathExpressionException {
        XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new HtmlNamespace());

        return (Element) xp.evaluate(xpath, dom, XPathConstants.NODE);
    }

    /**
     * Removes all the &lt;SCRIPT/&gt; tags from the document.
     *
     * @param dom the document object.
     * @return the changed dom.
     */
    public static Document removeScriptTags(Document dom) {
        return removeTags(dom, "SCRIPT");
    }

    /**
     * Removes all the given tags from the document.
     *
     * @param dom     the document object.
     * @param tagName the tag name, examples: script, style, meta
     * @return the changed dom.
     */
    public static Document removeTags(Document dom, String tagName) {
        NodeList list;
        try {
            list = XPathHelper.evaluateXpathExpression(dom,
                    "//" + tagName.toUpperCase());

            while (list.getLength() > 0) {
                Node sc = list.item(0);

                if (sc != null) {
                    sc.getParentNode().removeChild(sc);
                }

                list = XPathHelper.evaluateXpathExpression(dom,
                        "//" + tagName.toUpperCase());
            }
        } catch (XPathExpressionException e) {
            LOGGER.error("Error while removing tag " + tagName, e);
        }

        return dom;

    }

    /**
     * @param dom the DOM document.
     * @return a string representation of the DOM.
     */
    public static String getDocumentToString(Document dom) {
        try {
            Source source = new DOMSource(dom);
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(stringWriter);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                    "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "html");
            transformer.transform(source, result);
            return stringWriter.getBuffer().toString();
        } catch (TransformerException e) {
            throw new CrawljaxException("Could not transform the DOM", e);
        }

    }

    /**
     * Serialize the Document object.
     *
     * @param dom the document to serialize
     * @return the serialized dom String
     */
    public static byte[] getDocumentToByteArray(Document dom) {
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();

            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer
                    .setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "html");
            // TODO should be fixed to read doctype declaration
            transformer
                    .setOutputProperty(
                            OutputKeys.DOCTYPE_PUBLIC,
                            "-//W3C//DTD XHTML 1.0 Strict//EN\" "
                                    + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd");

            DOMSource source = new DOMSource(dom);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Result result = new StreamResult(out);
            transformer.transform(source, result);

            return out.toByteArray();
        } catch (TransformerException e) {
            LOGGER.error("Error while converting the document to a byte array",
                    e);
        }
        return null;

    }

    /**
     * Returns the text value of an element (title, alt or contents). Note that the result is 50
     * characters or less in length.
     *
     * @param element The element.
     * @return The text value of the element.
     */
    public static String getTextValue(Element element) {
        String ret = "";
        String textContent = element.getTextContent();
        if (textContent != null && !textContent.equals("")) {
            ret = textContent;
        } else if (element.hasAttribute("title")) {
            ret = element.getAttribute("title");
        } else if (element.hasAttribute("alt")) {
            ret = element.getAttribute("alt");
        }
        if (ret.length() > TEXT_CUTOFF) {
            return ret.substring(0, TEXT_CUTOFF);
        } else {
            return ret;
        }
    }

    /**
     * Get differences between DOMs.
     *
     * @param controlDom The control dom.
     * @param testDom    The test dom.
     * @return The differences.
     */
    public static List<Difference> getDifferences(String controlDom,
                                                  String testDom) {
        return getDifferences(controlDom, testDom,
                Lists.newArrayList());
    }

    /**
     * Get differences between DOMs.
     *
     * @param controlDom       The control dom.
     * @param testDom          The test dom.
     * @param ignoreAttributes The list of attributes to ignore.
     * @return The differences.
     */
    @SuppressWarnings("unchecked")
    public static List<Difference> getDifferences(String controlDom,
                                                  String testDom, final List<String> ignoreAttributes) {
        try {
            Diff d = new Diff(DomUtils.asDocument(controlDom),
                    DomUtils.asDocument(testDom));
            DetailedDiff dd = new DetailedDiff(d);
            dd.overrideDifferenceListener(new DomDifferenceListener(
                    ignoreAttributes));

            return dd.getAllDifferences();
        } catch (IOException e) {
            LOGGER.error("Error with getDifferences: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Removes newlines from a string.
     *
     * @param html The string.
     * @return The new string without the newlines or tabs.
     */
    public static String removeNewLines(String html) {
        return html.replaceAll("[\\t\\n\\x0B\\f\\r]", "");
    }

    /**
     * @param string  The original string.
     * @param regex   The regular expression.
     * @param replace What to replace it with.
     * @return replaces regex in str by replace where the dot sign also supports newlines
     */
    public static String replaceString(String string, String regex,
                                       String replace) {
        Pattern p = Pattern.compile(regex, Pattern.DOTALL);
        Matcher m = p.matcher(string);
        String replaced = m.replaceAll(replace);
        p = Pattern.compile("  ", Pattern.DOTALL);
        m = p.matcher(replaced);
        return m.replaceAll(" ");
    }

    /**
     * Adds a slash to a path if it doesn't end with a slash.
     *
     * @param folderName The path to append a possible slash.
     * @return The new, correct path.
     */
    public static String addFolderSlashIfNeeded(String folderName) {
        if (!"".equals(folderName) && !folderName.endsWith("/")) {
            return folderName + "/";
        } else {
            return folderName;
        }
    }

    /**
     * Returns the filename in a path. For example with path = "foo/bar/crawljax.txt" returns
     * "crawljax.txt"
     *
     * @param path
     * @return the filename from the path
     */
    private static String getFileNameInPath(String path) {
        String fName;
        if (path.indexOf('/') != -1) {
            fName = path.substring(path.lastIndexOf('/') + 1);
        } else {
            fName = path;
        }
        return fName;
    }

    /**
     * Retrieves the content of the filename. Also reads from JAR Searches for the resource in the
     * root folder in the jar
     *
     * @param fileName Filename.
     * @return The contents of the file.
     * @throws IOException On error.
     */
    public static String getTemplateAsString(String fileName) throws IOException {
        // in .jar file
        String fNameJar = getFileNameInPath(fileName);
        InputStream inStream = DomUtils.class.getResourceAsStream("/"
                + fNameJar);
        if (inStream == null) {
            // try to find file normally
            File f = new File(fileName);
            if (f.exists()) {
                inStream = new FileInputStream(f);
            } else {
                throw new IOException("Cannot find " + fileName + " or "
                        + fNameJar);
            }
        }

        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(inStream));
        String line;
        StringBuilder stringBuilder = new StringBuilder();

        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }

        bufferedReader.close();
        return stringBuilder.toString();
    }

    /**
     * @param frame the frame element.
     * @return the name or id of this element if they are present, otherwise null.
     */
    public static String getFrameIdentification(Element frame) {

        Attr attr = frame.getAttributeNode("id");
        if (attr != null && attr.getNodeValue() != null
                && !attr.getNodeValue().equals("")) {
            return attr.getNodeValue();
        }

        attr = frame.getAttributeNode("name");
        if (attr != null && attr.getNodeValue() != null
                && !attr.getNodeValue().equals("")) {
            return attr.getNodeValue();
        }

        return null;

    }

    /**
     * Write the document object to a file.
     *
     * @param document     the document object.
     * @param filePathname the path name of the file to be written to.
     * @param method       the output method: for instance html, xml, text
     * @param indent       amount of indentation. -1 to use the default.
     * @throws TransformerException if an exception occurs.
     * @throws IOException          if an IO exception occurs.
     */
    public static void writeDocumentToFile(Document document,
                                           String filePathname, String method, int indent)
            throws TransformerException, IOException {

        Transformer transformer = TransformerFactory.newInstance()
                .newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, method);

        transformer.transform(new DOMSource(document), new StreamResult(
                new FileOutputStream(filePathname)));
    }

    private DomUtils() {
    }

    public static NodeList getAllLeafNodes(Document document) throws XPathExpressionException {
        XPathExpression xpath =
                XPathFactory.newInstance().newXPath().compile("//*[count(./*) = 0]");
        return (NodeList) xpath.evaluate(document, XPathConstants.NODESET);
    }

    public static String getAttributeFromElement(Node element, String attribute) {
        if (element.getAttributes() != null) {
            if (element.getAttributes().getNamedItem(attribute) != null)
                return element.getAttributes().getNamedItem(attribute).getTextContent();
        }
        return null;
    }

    public static List<Node> getElementsByTagName(Document document, String tagName) {
        List<Node> returnList = new ArrayList<>();
        NodeList nodes = document.getElementsByTagName(tagName);
        for (int i = 0; i < nodes.getLength(); i++) {
            returnList.add(nodes.item(i));
        }
        return returnList;
    }

    public static List<String> getAllImageURLs(Document document) {
        List<String> imgURLs = new ArrayList<>();
        List<Node> imgNodes = getElementsByTagName(document, "img");
        imgNodes.addAll(getElementsByTagName(document, "IMG"));
        imgNodes.addAll(getElementsByTagName(document, "image"));
        imgNodes.addAll(getElementsByTagName(document, "IMAGE"));

        for (Node imgNode : imgNodes) {
            String src = getAttributeFromElement(imgNode, "src");
            if (src == null) {
                src = getAttributeFromElement(imgNode, "SRC");
            }
            if (src != null) {
                src = src.trim().replaceAll("\"", "");
                imgURLs.add(src);
            }
        }
        return imgURLs;
    }

    /**
     * To get all the textual content in the dom
     *
     * @param document
     * @param individualTokens : default True : when set to true, each text node from dom is used to build the
     *                         text content : when set to false, the text content of whole is obtained at once.
     * @return
     */
    public static String getTextContent(Document document, boolean individualTokens) {
        String textContent = null;
        if (individualTokens) {
            List<String> tokens = getTextTokens(document);
            textContent = StringUtils.join(tokens, ",");
        } else {
            textContent =
                    document.getDocumentElement().getTextContent().trim().replaceAll("\\s+", ",");
        }
        return textContent;
    }

    public static List<String> getTextTokens(Document document) {
        List<String> tokens = new ArrayList<>();
        XPathFactory xpathFactory = XPathFactory.newInstance();
        // XPath to find empty text nodes.
        XPathExpression xpathExp;
        try {
            xpathExp = xpathFactory.newXPath().compile(
                    "//text()");
            NodeList textNodes = (NodeList) xpathExp.evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < textNodes.getLength(); i++) {
                Node textNode = textNodes.item(i);
                if (textNode.getNodeValue().trim().isEmpty())
                    continue;
                tokens.add(textNode.getNodeValue().trim());
            }
        } catch (XPathExpressionException e) {

        }
        return tokens;
    }

    public static String getDOMContent(Document document) {
        String textContent = getTextContent(document, true);
        String content = textContent;
        String imageContent = StringUtils.join(getAllImageURLs(document), ",");
        content = textContent + "," + imageContent;
        return content;
    }

    public static String getDOMContent(String dom) {
        Document doc;
        try {
            doc = DomUtils.asDocument(dom);
            return getDOMContent(doc);
        } catch (IOException e) {
            LOGGER.info("IOException while creating document from dom string");
            LOGGER.debug(dom);
        }
        return null;
    }

    public static String getDOMWithoutContent(Document document) throws XPathExpressionException {
        XPathFactory xpathFactory = XPathFactory.newInstance();
        // XPath to find empty text nodes.
        XPathExpression xpathExp = xpathFactory.newXPath().compile("//text()");
        NodeList textNodes = (NodeList) xpathExp.evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < textNodes.getLength(); i++) {
            Node textNode = textNodes.item(i);
            textNode.getParentNode().removeChild(textNode);
        }
        return getDocumentToString(document);
    }

    public static String getDOMWithoutContent(String dom) {
        try {
            Document doc = asDocument(dom);
            return getDOMWithoutContent(doc);
        } catch (IOException e) {
            LOGGER.info("Error converting dom string to Document");
            LOGGER.debug(dom);
        } catch (XPathExpressionException e) {
            LOGGER.error("Error removing text nodes from DOM");
            LOGGER.debug(dom);
        }
        return null;
    }

}
