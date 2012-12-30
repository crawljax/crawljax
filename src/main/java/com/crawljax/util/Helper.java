package com.crawljax.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
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

import com.google.common.collect.Lists;

/**
 * Utility class that contains a number of helper functions used by Crawljax and some plugins.
 * 
 * @author mesbah
 * @version $Id$
 */
public final class Helper {

	private static final int BASE_LENGTH = 3;

	private static final int TEXT_CUTOFF = 50;

	public static final Logger LOGGER = LoggerFactory.getLogger(Helper.class.getName());

	private Helper() {
	}

	/**
	 * Internal used function to strip the basePath from a given url.
	 * 
	 * @param url
	 *            the url to examine
	 * @return the base path with file stipped
	 */
	private static String getBasePath(URL url) {
		String file = url.getFile().replaceAll("\\*", "");

		try {
			return url.getPath().replaceAll(file, "");
		} catch (PatternSyntaxException pe) {
			LOGGER.error(pe.getMessage());
			return "";
		}

	}

	/**
	 * @param location
	 *            Current location.
	 * @param link
	 *            Link to check.
	 * @return Whether location and link are on the same domain.
	 */
	public static boolean isLinkExternal(String location, String link) {

		if (!location.contains("://")) {
			// location must always contain :// by rule, it not link is handled as not external
			return false;
		}

		// This will jump out of the local file location
		if (location.startsWith("file") && link.startsWith("/")) {
			return true;
		}

		if (link.contains("://")) {
			if (location.startsWith("file") && link.startsWith("http") || link.startsWith("file")
			        && location.startsWith("http")) {
				// Jump from file to http(s) or from http(s) to file, so external
				return true;
			}
			try {
				URL locationUrl = new URL(location);
				try {
					URL linkUrl = new URL(link);
					if (linkUrl.getHost().equals(locationUrl.getHost())) {
						String linkPath = getBasePath(linkUrl);
						return !(linkPath.startsWith(getBasePath(locationUrl)));
					}
					return true;
				} catch (MalformedURLException e) {
					LOGGER.info("Can not parse link " + link + " to check its externalOf "
					        + location);
					return false;
				}
			} catch (MalformedURLException e) {
				LOGGER.info("Can not parse location " + location + " to check if " + link
				        + " isExternal", e);
				return false;
			}
		} else {
			// No full url specifier so internal link...
			return false;
		}
	}

	/**
	 * @param url
	 *            the URL string.
	 * @return the base part of the URL.
	 */
	public static String getBaseUrl(String url) {
		String head = url.substring(0, url.indexOf(":"));
		String subLoc = url.substring(head.length() + BASE_LENGTH);
		return head + "://" + subLoc.substring(0, subLoc.indexOf("/"));
	}

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
	public static Document getDocument(String html) throws SAXException, IOException {
		DOMParser domParser = new DOMParser();
		domParser.setProperty("http://cyberneko.org/html/properties/names/elems", "match");
		domParser.setFeature("http://xml.org/sax/features/namespaces", false);
		domParser.parse(new InputSource(new StringReader(html)));
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
		return getElementAttributes(element, new ArrayList<String>());
	}

	/**
	 * @param element
	 *            The DOM Element.
	 * @param exclude
	 *            the list of exclude strings.
	 * @return A string representation of the element's attributes excluding exclude.
	 */
	public static String getElementAttributes(Element element, List<String> exclude) {
		StringBuffer buffer = new StringBuffer();

		if (element != null) {
			NamedNodeMap attributes = element.getAttributes();
			if (attributes != null) {
				for (int i = 0; i < attributes.getLength(); i++) {
					Attr attr = (Attr) attributes.item(i);
					if (!exclude.contains(attr.getNodeName())) {
						buffer.append(attr.getNodeName() + "=");
						buffer.append(attr.getNodeValue() + " ");
					}
				}
			}
		}

		return buffer.toString().trim();
	}

	/**
	 * @param element
	 *            the element.
	 * @return a string representation of the element including its attributes.
	 */
	public static String getElementString(Element element) {
		if (element == null) {
			return "";
		}
		String text = Helper.removeNewLines(Helper.getTextValue(element)).trim();
		String info = "";
		if (!text.equals("")) {
			info += "\"" + text + "\" ";
			// Helper.removeNewLines(this.text.trim()) + " - ";
		}
		if (element != null) {
			if (element.hasAttribute("id")) {
				info += "ID: " + element.getAttribute("id") + " ";
			}
			info += Helper.getAllElementAttributes(element) + " ";
		}
		return info;
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
		if (dom != null) {
			// NodeList list = dom.getElementsByTagName("SCRIPT");

			NodeList list;
			try {
				list = XPathHelper.evaluateXpathExpression(dom, "//" + tagName.toUpperCase());

				while (list.getLength() > 0) {
					Node sc = list.item(0);

					if (sc != null) {
						sc.getParentNode().removeChild(sc);
					}

					list = XPathHelper.evaluateXpathExpression(dom, "//" + tagName.toUpperCase());
					// list = dom.getElementsByTagName("SCRIPT");
				}
			} catch (XPathExpressionException e) {
				LOGGER.error(e.getMessage(), e);
			}

			return dom;
		}

		return null;
	}

	/**
	 * Checks the existence of the directory. If it does not exist, the method creates it.
	 * 
	 * @param dir
	 *            the directory to check.
	 * @throws IOException
	 *             if fails.
	 */
	public static void directoryCheck(String dir) throws IOException {
		final File file = new File(dir);

		if (!file.exists()) {
			FileUtils.forceMkdir(file);
		}
	}

	/**
	 * Checks whether the folder exists for fname, and creates it if neccessary.
	 * 
	 * @param fname
	 *            folder name.
	 * @throws IOException
	 *             an IO exception.
	 */
	public static void checkFolderForFile(String fname) throws IOException {

		if (fname.lastIndexOf(File.separator) > 0) {
			String folder = fname.substring(0, fname.lastIndexOf(File.separator));
			Helper.directoryCheck(folder);
		}
	}

	/**
	 * Retrieve the var value for varName from a HTTP query string (format is
	 * "var1=val1&var2=val2").
	 * 
	 * @param varName
	 *            the name.
	 * @param haystack
	 *            the haystack.
	 * @return variable value for varName
	 */
	public static String getVarFromQueryString(String varName, String haystack) {
		if (haystack == null || haystack.length() == 0) {
			return null;
		}
		if (haystack.charAt(0) == '?') {
			haystack = haystack.substring(1);
		}
		String[] vars = haystack.split("&");

		for (String var : vars) {
			String[] tuple = var.split("=");
			if (tuple.length == 2 && tuple[0].equals(varName)) {
				return tuple[1];
			}
		}
		return null;
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
		} catch (TransformerConfigurationException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (TransformerException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return null;

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
			// transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,
			// "-//W3C//DTD XHTML 1.0 Transitional//EN\"
			// \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");
			transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,
			        "-//W3C//DTD XHTML 1.0 Strict//EN\" "
			                + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd");

			// transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,
			// "-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd");
			DOMSource source = new DOMSource(dom);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Result result = new StreamResult(out);
			transformer.transform(source, result);

			// System.out.println("Injected Javascript!");
			return out.toByteArray();
		} catch (TransformerConfigurationException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (TransformerException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return null;

	}

	/**
	 * Save a string to a file and append a newline character to that string.
	 * 
	 * @param filename
	 *            The filename to save to.
	 * @param text
	 *            The text to save.
	 * @param append
	 *            Whether to append to existing file.
	 * @throws IOException
	 *             On error.
	 */
	public static void writeToFile(String filename, String text, boolean append)
	        throws IOException {
		FileWriter fw = new FileWriter(filename, append);
		try {
			fw.write(text + "\n");
		} catch (IOException e) {
			throw e;
		} finally {
			fw.close();
		}
	}

	/**
	 * @param code
	 *            hashcode.
	 * @return String version of hashcode.
	 */
	public static String hashCodeToString(long code) {
		if (code < 0) {
			return "0" + (code * -1);
		} else {
			return "" + code;
		}
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
		if (element == null) {
			return "";
		}

		if (element.getTextContent() != null) {
			ret = element.getTextContent();
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
			Diff d = new Diff(Helper.getDocument(controlDom), Helper.getDocument(testDom));
			DetailedDiff dd = new DetailedDiff(d);
			dd.overrideDifferenceListener(new DifferenceListener() {

				@Override
				public void skippedComparison(Node control, Node test) {
				}

				@Override
				public int differenceFound(Difference difference) {
					if (difference.getControlNodeDetail() == null
					        || difference.getControlNodeDetail().getNode() == null
					        || difference.getTestNodeDetail() == null
					        || difference.getTestNodeDetail().getNode() == null) {
						return RETURN_ACCEPT_DIFFERENCE;
					}
					if (ignoreAttributes.contains(difference.getTestNodeDetail().getNode()
					        .getNodeName())
					        || ignoreAttributes.contains(difference.getControlNodeDetail()
					                .getNode().getNodeName())) {
						return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
					}
					return RETURN_ACCEPT_DIFFERENCE;
				}
			});

			return dd.getAllDifferences();
		} catch (Exception e) {
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
		if (!folderName.equals("") && !folderName.endsWith("/")) {
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
		if (path.indexOf("/") != -1) {
			fname = path.substring(path.lastIndexOf("/") + 1);
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
		InputStream inStream = Helper.class.getResourceAsStream("/" + fnameJar);
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
		        ""
		                + "function ATUSA_getElementInNodes(nodes, tagName, number){"
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

		checkFolderForFile(filePathname);
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, method);

		if (indent > -1) {
			transformer.setOutputProperty(
			        org.apache.xml.serializer.OutputPropertiesFactory.S_KEY_INDENT_AMOUNT,
			        Integer.toString(indent));
		}
		transformer.transform(new DOMSource(document), new StreamResult(new FileOutputStream(
		        filePathname)));
	}

	/**
	 * Returns the file contents without stripping line-endings.
	 * 
	 * @param file
	 *            File to read out.
	 * @return Contents including line-endings.
	 */
	public static String getContent(File file) {
		StringBuilder contents = new StringBuilder();

		try {
			BufferedReader input = new BufferedReader(new FileReader(file));
			try {
				String line = null; // not declared within while loop
				while ((line = input.readLine()) != null) {
					contents.append(line);
					contents.append("\n");
				}
			} finally {
				input.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return contents.toString();
	}

}
