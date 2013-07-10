package com.crawljax.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.cyberneko.html.parsers.DOMParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.crawljax.core.CrawljaxException;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * Utility class that contains a number of helper functions used by Crawljax and some plugins.
 */
public final class DomUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(DomUtils.class.getName());

	static final int BASE_LENGTH = 3;

	private static final int TEXT_CUTOFF = 50;

	/**
	 * transforms a string into a Document object. TODO This needs more optimizations. As it seems
	 * the getDocument is called way too much times causing a lot of parsing which is slow and not
	 * necessary.
	 * 
	 * @param html
	 *            the HTML string.
	 * @return The DOM Document version of the HTML string.
	 * @throws IOException
	 *             if an IO failure occurs.
	 * @throws SAXException
	 *             if an exception occurs while parsing the HTML string.
	 */
	public static Document asDocument(String html) throws IOException {
		DOMParser domParser = new DOMParser();
		try {
			domParser.setProperty("http://cyberneko.org/html/properties/names/elems", "match");
			domParser.setFeature("http://xml.org/sax/features/namespaces", false);
			domParser.parse(new InputSource(new StringReader(html)));
		} catch (SAXException e) {
			throw new IOException("Error while reading HTML: " + html, e);
		}
		return domParser.getDocument();
	}

	/**
	 * @param html
	 *            the HTML string.
	 * @return a Document object made from the HTML string.
	 * @throws SAXException
	 *             if an exception occurs while parsing the HTML string.
	 * @throws IOException
	 *             if an IO failure occurs.
	 */
	public static Document getDocumentNoBalance(String html) throws SAXException, IOException {
		DOMParser domParser = new DOMParser();
		domParser.setProperty("http://cyberneko.org/html/properties/names/elems", "match");
		domParser.setFeature("http://cyberneko.org/html/features/balance-tags", false);
		domParser.parse(new InputSource(new StringReader(html)));
		return domParser.getDocument();
	}

	/**
	 * @param element
	 *            The DOM Element.
	 * @return A string representation of all the element's attributes.
	 */
	public static String getAllElementAttributes(Element element) {
		return getElementAttributes(element, ImmutableSet.<String> of());
	}

	/**
	 * @param element
	 *            The DOM Element.
	 * @param exclude
	 *            the list of exclude strings.
	 * @return A string representation of the element's attributes excluding exclude.
	 */
	public static String getElementAttributes(Element element, ImmutableSet<String> exclude) {
		StringBuilder buffer = new StringBuilder();
		if (element != null) {
			NamedNodeMap attributes = element.getAttributes();
			if (attributes != null) {
				addAttributesToString(exclude, buffer, attributes);
			}
		}

		return buffer.toString().trim();
	}

	private static void addAttributesToString(ImmutableSet<String> exclude, StringBuilder buffer,
	        NamedNodeMap attributes) {
		for (int i = 0; i < attributes.getLength(); i++) {
			Attr attr = (Attr) attributes.item(i);
			if (!exclude.contains(attr.getNodeName())) {
				buffer.append(attr.getNodeName()).append('=');
				buffer.append(attr.getNodeValue()).append(' ');
			}
		}
	}

	/**
	 * @param element
	 *            the element.
	 * @return a string representation of the element including its attributes.
	 */
	public static String getElementString(Element element) {
		String text = DomUtils.removeNewLines(DomUtils.getTextValue(element)).trim();
		StringBuilder info = new StringBuilder();
		if (!Strings.isNullOrEmpty(text)) {
			info.append("\"").append(text).append("\" ");
		}
		if (element != null) {
			if (element.hasAttribute("id")) {
				info.append("ID: ").append(element.getAttribute("id")).append(" ");
			}
			info.append(DomUtils.getAllElementAttributes(element)).append(" ");
		}
		return info.toString();
	}

	/**
	 * @param dom
	 *            the DOM document.
	 * @param xpath
	 *            the xpath.
	 * @return The element found on DOM having the xpath position.
	 * @throws XPathExpressionException
	 *             if the xpath fails.
	 */
	public static Element getElementByXpath(Document dom, String xpath)
	        throws XPathExpressionException {
		XPath xp = XPathFactory.newInstance().newXPath();
		xp.setNamespaceContext(new HtmlNamespace());

		return (Element) xp.evaluate(xpath, dom, XPathConstants.NODE);
	}

	/**
	 * Removes all the <SCRIPT/> tags from the document.
	 * 
	 * @param dom
	 *            the document object.
	 * @return the changed dom.
	 */
	public static Document removeScriptTags(Document dom) {
		return removeTags(dom, "SCRIPT");
	}

	/**
	 * Removes all the given tags from the document.
	 * 
	 * @param dom
	 *            the document object.
	 * @param tagName
	 *            the tag name, examples: script, style, meta
	 * @return the changed dom.
	 */
	public static Document removeTags(Document dom, String tagName) {
		NodeList list;
		try {
			list = XPathHelper.evaluateXpathExpression(dom, "//" + tagName.toUpperCase());

			while (list.getLength() > 0) {
				Node sc = list.item(0);

				if (sc != null) {
					sc.getParentNode().removeChild(sc);
				}

				list = XPathHelper.evaluateXpathExpression(dom, "//" + tagName.toUpperCase());
			}
		} catch (XPathExpressionException e) {
			LOGGER.error("Error while removing tag " + tagName, e);
		}

		return dom;

	}

	/**
	 * @param dom
	 *            the DOM document.
	 * @return a string representation of the DOM.
	 */
	public static String getDocumentToString(Document dom) {
		try {
			Source source = new DOMSource(dom);
			StringWriter stringWriter = new StringWriter();
			Result result = new StreamResult(stringWriter);
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "html");
			transformer.transform(source, result);
			return stringWriter.getBuffer().toString();
		} catch (TransformerException e) {
			throw new CrawljaxException("Could not tranform the DOM", e);
		}

	}

	/**
	 * Serialize the Document object.
	 * 
	 * @param dom
	 *            the document to serialize
	 * @return the serialized dom String
	 */
	public static byte[] getDocumentToByteArray(Document dom) {
		try {
			TransformerFactory tFactory = TransformerFactory.newInstance();

			Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "html");
			// TODO should be fixed to read doctype declaration
			transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,
			        "-//W3C//DTD XHTML 1.0 Strict//EN\" "
			                + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd");

			DOMSource source = new DOMSource(dom);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Result result = new StreamResult(out);
			transformer.transform(source, result);

			return out.toByteArray();
		} catch (TransformerException e) {
			LOGGER.error("Error while converting the document to a byte array", e);
		}
		return null;

	}

	/**
	 * Returns the text value of an element (title, alt or contents). Note that the result is 50
	 * characters or less in length.
	 * 
	 * @param element
	 *            The element.
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
	 * Get differences between doms.
	 * 
	 * @param controlDom
	 *            The control dom.
	 * @param testDom
	 *            The test dom.
	 * @return The differences.
	 */
	public static List<Difference> getDifferences(String controlDom, String testDom) {
		return getDifferences(controlDom, testDom, Lists.<String> newArrayList());
	}

	/**
	 * Get differences between doms.
	 * 
	 * @param controlDom
	 *            The control dom.
	 * @param testDom
	 *            The test dom.
	 * @param ignoreAttributes
	 *            The list of attributes to ignore.
	 * @return The differences.
	 */
	@SuppressWarnings("unchecked")
	public static List<Difference> getDifferences(String controlDom, String testDom,
	        final List<String> ignoreAttributes) {
		try {
			Diff d = new Diff(DomUtils.asDocument(controlDom), DomUtils.asDocument(testDom));
			DetailedDiff dd = new DetailedDiff(d);
			dd.overrideDifferenceListener(new DomDifferenceListener(ignoreAttributes));

			return dd.getAllDifferences();
		} catch (IOException e) {
			LOGGER.error("Error with getDifferences: " + e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Removes newlines from a string.
	 * 
	 * @param html
	 *            The string.
	 * @return The new string without the newlines or tabs.
	 */
	public static String removeNewLines(String html) {
		return html.replaceAll("[\\t\\n\\x0B\\f\\r]", "");
	}

	/**
	 * @param string
	 *            The original string.
	 * @param regex
	 *            The regular expression.
	 * @param replace
	 *            What to replace it with.
	 * @return replaces regex in str by replace where the dot sign also supports newlines
	 */
	public static String replaceString(String string, String regex, String replace) {
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
	 * @param folderName
	 *            The path to append a possible slash.
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
		String fname;
		if (path.indexOf('/') != -1) {
			fname = path.substring(path.lastIndexOf('/') + 1);
		} else {
			fname = path;
		}
		return fname;
	}

	/**
	 * Retrieves the content of the filename. Also reads from JAR Searches for the resource in the
	 * root folder in the jar
	 * 
	 * @param fname
	 *            Filename.
	 * @return The contents of the file.
	 * @throws IOException
	 *             On error.
	 */
	public static String getTemplateAsString(String fname) throws IOException {
		// in .jar file
		String fnameJar = getFileNameInPath(fname);
		InputStream inStream = DomUtils.class.getResourceAsStream("/" + fnameJar);
		if (inStream == null) {
			// try to find file normally
			File f = new File(fname);
			if (f.exists()) {
				inStream = new FileInputStream(f);
			} else {
				throw new IOException("Cannot find " + fname + " or " + fnameJar);
			}
		}

		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inStream));
		String line;
		StringBuilder stringBuilder = new StringBuilder();

		while ((line = bufferedReader.readLine()) != null) {
			stringBuilder.append(line + "\n");
		}

		bufferedReader.close();
		return stringBuilder.toString();
	}

	/**
	 * @param xpath
	 *            The xpath of the element.
	 * @return The JavaScript to get an element.
	 */
	public static String getJSGetElement(String xpath) {
		String js =
		        "function ATUSA_getElementInNodes(nodes, tagName, number){"
		                + "try{"
		                + "var pos = 1;"
		                + "for(i=0; i<nodes.length; i++){"
		                + "if(nodes[i]!=null && nodes[i].tagName!=null && "
		                + "nodes[i].tagName.toLowerCase() == tagName){"
		                + "if(number==pos){"
		                + "return nodes[i];"
		                + "}else{"
		                + "pos++;"
		                + "}"
		                + "}"
		                + "}"
		                + "}catch(e){}"
		                + "return null;"
		                + "}"
		                + "function ATUSA_getElementByXpath(xpath){"
		                + "try{"
		                + "var elements = xpath.toLowerCase().split('/');"
		                + "var curNode = window.document.body;"
		                + "var tagName, number;"
		                + "for(j=0; j<elements.length; j++){"
		                + "if(elements[j]!=''){"
		                + "if(elements[j].indexOf('[')==-1){"
		                + "tagName = elements[j];"
		                + "number = 1;"
		                + "}else{"
		                + "tagName = elements[j].substring(0, elements[j].indexOf('['));"
		                + "number = elements[j].substring(elements[j].indexOf('[')+1, "
		                + "elements[j].lastIndexOf(']'));"
		                + "}"
		                + "if(tagName!='body' && tagName!='html'){"
		                + "curNode = ATUSA_getElementInNodes(curNode.childNodes, tagName, number);"
		                + "if(curNode==null){" + "return null;" + "}" + "}" + "}" + "}"
		                + "}catch(e){return null;}" + "return curNode;" + "}"
		                + "try{var ATUSA_element = ATUSA_getElementByXpath('" + xpath
		                + "');}catch(e){return null;}";

		return js;
	}

	/**
	 * @param frame
	 *            the frame element.
	 * @return the name or id of this element if they are present, otherwise null.
	 */
	public static String getFrameIdentification(Element frame) {

		Attr attr = frame.getAttributeNode("id");
		if (attr != null && attr.getNodeValue() != null && !attr.getNodeValue().equals("")) {
			return attr.getNodeValue();
		}

		attr = frame.getAttributeNode("name");
		if (attr != null && attr.getNodeValue() != null && !attr.getNodeValue().equals("")) {
			return attr.getNodeValue();
		}

		return null;

	}

	/**
	 * Write the document object to a file.
	 * 
	 * @param document
	 *            the document object.
	 * @param filePathname
	 *            the path name of the file to be written to.
	 * @param method
	 *            the output method: for instance html, xml, text
	 * @param indent
	 *            amount of indentation. -1 to use the default.
	 * @throws TransformerException
	 *             if an exception occurs.
	 * @throws IOException
	 *             if an IO exception occurs.
	 */
	public static void writeDocumentToFile(Document document, String filePathname, String method,
	        int indent) throws TransformerException, IOException {

		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, method);

		transformer.transform(new DOMSource(document), new StreamResult(new FileOutputStream(
		        filePathname)));
	}

	private DomUtils() {
	}

}
