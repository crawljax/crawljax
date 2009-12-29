/**
 * Created Oct 16, 2007
 */
package com.crawljax.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.crawljax.browser.EmbeddedBrowser;

/**
 * @author mesbah
 * @version $Id: Helper.java 6339 2009-12-28 11:46:49Z danny $
 */
public final class Helper {

	public static final String[] DEFAULT_STRIP_ATTRIBUTES =
	        { "closure_hashcode_(\\w)*", "jquery[0-9]+" };

	// TODO: ali, remove variables below?
	private static final int BASE_LENGTH = 3;

	private static final int START_LENGTH = 7;

	private static final int TEXT_CUTOFF = 50;

	public static final Logger LOGGER = Logger.getLogger(Helper.class.getName());

	private Helper() {
	}

	/**
	 * @param location
	 *            DOCUMENT ME!
	 * @param link
	 *            DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static boolean isLinkExternal(String location, String link) {
		boolean check = false;
		if (location.startsWith("file") && link.startsWith("http")) {
			return true;
		}

		if (link.startsWith("http") && location.startsWith("http")) {
			String subLink = link.substring(START_LENGTH);
			int index = subLink.indexOf("/");

			if (index > -1) {
				subLink = subLink.substring(0, index);
			}

			String subLoc = location.substring(START_LENGTH);
			index = subLoc.indexOf("/");

			if (index > -1) {
				subLoc = subLoc.substring(0, subLoc.indexOf("/"));
			}

			if (!subLoc.equals(subLink)) {
				check = true;
			}

			LOGGER.debug(subLink);
		}

		return check;
	}

	/**
	 * Saves content in file. Creates the folder if it does not exists
	 * 
	 * @param content
	 *            file content
	 * @param fileName
	 *            the file name.
	 * @throws IOException
	 *             if fails.
	 */
	public static void saveFile(String content, String fileName) throws IOException {
		checkFolderForFile(fileName);
		FileWriter out = new FileWriter(new File(fileName));
		out.write(content);
		out.close();
	}

	/**
	 * @param url
	 *            the URL string.
	 * @return the base part of the URL.
	 */
	public static String getBaseUrl(String url) {
		String temp = new String(url);
		String head = temp.substring(0, temp.indexOf(":"));
		String subLoc = url.substring(head.length() + BASE_LENGTH);

		return head + "://" + subLoc.substring(0, subLoc.indexOf("/"));
	}

	/**
	 * transforms a string into a Document object.
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

			for (int i = 0; i < attributes.getLength(); i++) {
				Attr attr = (Attr) attributes.item(i);
				if (!exclude.contains(attr.getNodeName())) {
					buffer.append(attr.getNodeName() + "=");
					buffer.append(attr.getNodeValue() + " ");
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
	 * @param document
	 *            The Document object.
	 * @param filePathname
	 *            the filename to write the document to.
	 */
	public static void writeDocumentToFile(Document document, String filePathname) {
		try {
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "text");

			DOMSource source = new DOMSource(document);
			Result result = new StreamResult(new FileOutputStream(filePathname));
			transformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (TransformerException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (FileNotFoundException e) {
			LOGGER.error(e.getMessage(), e);
		}
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
	 * @param dom
	 *            the DOM document.
	 * @param xpath
	 *            the xpath.
	 * @return The element found on DOM having the xpath position.
	 * @throws XPathExpressionException
	 *             if the xpath fails.
	 */
	public static NodeList getElementsByXpath(Document dom, String xpath)
	        throws XPathExpressionException {
		XPath xp = XPathFactory.newInstance().newXPath();
		return (NodeList) xp.evaluate(xpath, dom, XPathConstants.NODESET);
	}

	/**
	 * Removes all the <SCRIPT/> tags from the document.
	 * 
	 * @param dom
	 *            the document object.
	 * @return the changed dom.
	 */
	public static Document removeScriptTags(Document dom) {
		if (dom != null) {
			// NodeList list = dom.getElementsByTagName("SCRIPT");

			NodeList list;
			try {
				list = Helper.getElementsByXpath(dom, "//SCRIPT");

				while (list.getLength() > 0) {
					Node sc = list.item(0);

					if (sc != null) {
						sc.getParentNode().removeChild(sc);
					}

					list = Helper.getElementsByXpath(dom, "//SCRIPT");
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
	 * @param list
	 *            the list of numbers.
	 * @return the mean of the numbers in the list.
	 */

	public static double calculateMean(List<Integer> list) {
		final Mean mean = new Mean();

		for (Integer num : list) {
			mean.increment(num.intValue());
		}

		return mean.getResult();
	}

	/**
	 * @param str
	 *            DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static int countByte(String str) {
		return str.getBytes().length;
	}

	/**
	 * @param email
	 *            the string to check
	 * @return true if text has the email pattern.
	 */
	public static boolean isEmail(String email) {
		// Set the email pattern string
		final Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
		Matcher m = p.matcher(email);

		if (m.matches()) {
			return true;
		}

		return false;
	}

	/**
	 * @param href
	 *            the string to check
	 * @return true if href has the pdf or ps pattern.
	 */
	public static boolean isPDForPS(String href) {
		// Set the email pattern string
		final Pattern p = Pattern.compile(".+.pdf|.+.ps");
		Matcher m = p.matcher(href);

		if (m.matches()) {
			return true;
		}

		return false;
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
	 * Checks whether the folder exists for fname, and creates it if neccessary TODO: anyone, check,
	 * probably does not work correctly.
	 * 
	 * @param fname
	 *            folder name.
	 * @throws IOException
	 *             an IO exception.
	 */
	public static void checkFolderForFile(String fname) throws IOException {
		if (fname.lastIndexOf("/") > 0) {
			String folder = fname.substring(0, fname.lastIndexOf("/"));
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
	 * Save a string to a file.
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
		fw.write(text + "\n");
		fw.close();
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
		                + "var curNode = document.body;"
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
	@SuppressWarnings("unchecked")
	public static List<Difference> getDifferences(String controlDom, String testDom) {
		try {
			Diff d = new Diff(Helper.getDocument(controlDom), Helper.getDocument(testDom));
			DetailedDiff dd = new DetailedDiff(d);

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
	 * Get the contents of a file.
	 * 
	 * @param file
	 *            The name of the file.
	 * @return The contents as a String.
	 */
	public static String getContent(File file) {
		StringBuilder contents = new StringBuilder();

		try {
			BufferedReader input = new BufferedReader(new FileReader(file));
			try {
				String line = null; // not declared within while loop
				while ((line = input.readLine()) != null) {
					contents.append(line);
				}
			} finally {
				input.close();
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		return contents.toString();
	}

	// public static String getJavaScriptEventHandlerFunctions() {
	// File f = new File("ATUSA_events.js");
	// String js = getContent(f)
	// + "supportDomEvent = "
	// + (PropertyHelper.getSupportDomEventsValue() ? "true" : "false")
	// + ";"
	// + "supportAddEvents = "
	// + (PropertyHelper.getSupportAddEventsValue() ? "true" : "false")
	// + ";" + "supportJQuery = "
	// + (PropertyHelper.getSupportJQueryValue() ? "true" : "false")
	// + ";";
	// return js;
	// }

	// public static void createEventWrappers(EmbeddedBrowser browser) {
	// return;
	// String js = getJavaScriptEventHandlerFunctions();
	// for (TagElement tag : PropertyHelper.getCrawlTagElements()) {
	// js += "ATUSA_wrapElements('" + tag.getName() + "');";
	// }
	// try {
	// browser.executeJavaScript(js);
	// } catch (Exception e) {
	// LOGGER.error("Error with wrapping event handlers: "
	// + e.getMessage(), e);
	// // System.exit(0);
	// }

	// }

	/**
	 * Filters attributes from the HTML string.
	 * 
	 * @param html
	 *            The HTML to filter.
	 * @return The filtered HTML string.
	 */
	public static String filterAttributes(String html) {
		if (PropertyHelper.getCrawlFilterAttributesValues() != null) {
			for (String attribute : PropertyHelper.getCrawlFilterAttributesValues()) {
				String regex = "\\s" + attribute + "=\"[^\"]*\"";
				Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
				Matcher m = p.matcher(html);
				html = m.replaceAll("");
			}
		}
		return html;
	}

	/**
	 * @param html
	 *            The html string.
	 * @return uniform version of dom with predefined attributes stripped
	 * @throws Exception
	 *             On error.
	 */
	public static String toUniformDOM(final String html) throws Exception {
		Pattern p =
		        Pattern.compile("<SCRIPT(.*?)</SCRIPT>", Pattern.DOTALL
		                | Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(html);
		String htmlFormatted = m.replaceAll("");

		p = Pattern.compile("<\\?xml:(.*?)>");
		m = p.matcher(html);
		htmlFormatted = m.replaceAll("");

		// html = html.replace("<?xml:namespace prefix = gwt >", "");

		Document doc = Helper.getDocument(htmlFormatted);
		htmlFormatted = Helper.getDocumentToString(doc);
		htmlFormatted = filterAttributes(htmlFormatted);
		return htmlFormatted;
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
		return m.replaceAll(replace);
	}

	/**
	 * @param browser
	 *            The browser.
	 * @return "return " if browser is not an IEBrowser instance. Needed for javascript code
	 */
	public static String useJSReturn(EmbeddedBrowser browser) {

		return "return ";

	}

	/**
	 * @param differences
	 *            List of differences.
	 * @param max
	 *            Max length of result.
	 * @return subset of size max of the differences list. Returns all differences when max=0
	 */
	public static List<Difference> subsetDifferences(List<Difference> differences, int max) {
		if (max > differences.size() || max == 0) {
			return differences;
		} else {
			return differences.subList(0, max);
		}
	}

	/**
	 * Adds a slash to a path if it doesn't end with a slash.
	 * 
	 * @param folderName
	 *            The path to append a possible slash.
	 * @return The new, correct path.
	 */
	public static String addFolderSlashIfNeeded(String folderName) {
		if (folderName.equals("") || !folderName.endsWith("/")) {
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
		InputStreamReader streamReader = new InputStreamReader(inStream);
		BufferedReader bufferedReader = new BufferedReader(streamReader);
		String line;
		StringBuilder stringBuilder = new StringBuilder();
		while ((line = bufferedReader.readLine()) != null) {
			stringBuilder.append(line + "\n");
		}
		return stringBuilder.toString();
	}

}
