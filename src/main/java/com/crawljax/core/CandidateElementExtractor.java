package com.crawljax.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.eventablecondition.EventableCondition;
import com.crawljax.condition.eventablecondition.EventableConditionChecker;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification;
import com.crawljax.forms.FormInputValueHelper;
import com.crawljax.util.Helper;
import com.crawljax.util.XPathHelper;

/**
 * This class extracts candidate elements from the DOM tree, based on the tags provided by the user.
 * Elements can also be excluded.
 * 
 * @author mesbah
 * @version $Id$
 */
public class CandidateElementExtractor {

	private static final Logger LOGGER =
	        Logger.getLogger(CandidateElementExtractor.class.getName());

	private final Crawler crawler;

	/**
	 * @param c
	 *            the Crawler which uses this Extractor
	 */
	public CandidateElementExtractor(Crawler c) {
		this.crawler = c;
	}

	/**
	 * This method extracts candidate elements from the current DOM tree in the browser, based on
	 * the crawl tags defined by the user.
	 * 
	 * @param crawlTagElements
	 *            a list of TagElements to include.
	 * @param crawlExcludeTagElements
	 *            a list of TagElements to exclude.
	 * @param clickOnce
	 *            true if each candidate elements should be included only once.
	 * @return a list of candidate elements that are not excluded.
	 * @throws CrawljaxException
	 *             if the method fails.
	 */
	public List<CandidateElement> extract(List<TagElement> crawlTagElements,
	        List<TagElement> crawlExcludeTagElements, boolean clickOnce) throws CrawljaxException {
		List<CandidateElement> tagElements = new ArrayList<CandidateElement>();

		crawler.requestExaminedElementsMutex();

		for (TagElement tag : crawlTagElements) {
			LOGGER.info("TAG: " + tag.toString());
			// TODO DELTA DIFF
			List<Element> temp;
			try {
				temp =
				        getNodeListForTagElement(this.crawler.getBrowser(), tag, this.crawler
				                .getEventableConditionChecker(), crawlExcludeTagElements);
			} catch (Exception e) {
				throw new CrawljaxException(e.getMessage(), e);
			}

			for (Element sourceElement : temp) {
				EventableCondition eventableCondition =
				        this.crawler.getEventableConditionChecker().getEventableCondition(
				                tag.getId());
				String xpath = XPathHelper.getXpathExpression(sourceElement);
				// get multiple candidate elements when there are input
				// fields connected to this element

				List<CandidateElement> candidateElements = new ArrayList<CandidateElement>();
				if (eventableCondition != null
				        && eventableCondition.getLinkedInputFields() != null
				        && eventableCondition.getLinkedInputFields().size() > 0) {
					// add multiple candidate elements, for every input
					// value combination
					candidateElements =
					        FormInputValueHelper.getCandidateElementsForInputs(this.crawler
					                .getBrowser(), sourceElement, eventableCondition);
				} else {
					// just add default element
					candidateElements.add(new CandidateElement(sourceElement, xpath));
				}

				addIframeTransitions(candidateElements);

				for (CandidateElement candidateElement : candidateElements) {
					String elementUniqueString = candidateElement.getUniqueString();
					if (!clickOnce || !crawler.elementIsAlreadyChecked(elementUniqueString)) {
						LOGGER.info("Found new candidate element: "
						        + candidateElement.getUniqueString());

						if (eventableCondition != null) {
							candidateElement.setEventableCondition(eventableCondition);
						}
						tagElements.add(candidateElement);
						// TODO add element to checkedElements after the
						// event is fired!
						crawler.markElementAsChecked(elementUniqueString);
						// also add string without 'atusa' attribute to
						// make sure an
						// form action element is only clicked for its
						// defined values
						crawler.markElementAsChecked(candidateElement.getGeneralString());
					}
				}
			}
		}

		crawler.releaseExaminedElementsMutex();

		LOGGER.info("Found " + tagElements.size() + " new candidate elements to analyze!");
		return tagElements;
	}

	private void addIframeTransitions(List<CandidateElement> candidateElements)
	        throws CrawljaxException {

		try {
			Document doc = Helper.getDocument(crawler.getBrowser().getDom());
			NodeList frameNodes = doc.getElementsByTagName("IFRAME");

			for (int i = 0; frameNodes != null && i < frameNodes.getLength(); i++) {
				CandidateElement candidateElement =
				        new CandidateElement((Element) frameNodes.item(i), "switchto.iframe",
				                new Identification("index", Integer.toString(i)));

				candidateElements.add(candidateElement);

			}

		} catch (Exception e) {
			throw new CrawljaxException(e.getMessage(), e);
		}

	}

	/**
	 * Returns a list of Elements form the DOM tree, matching the tag element.
	 * 
	 * @throws CrawljaxException
	 * @throws IOException
	 * @throws SAXException
	 * @throws XPathExpressionException
	 */
	private List<Element> getNodeListForTagElement(EmbeddedBrowser browser,
	        TagElement tagElement, EventableConditionChecker eventableConditionChecker,
	        List<TagElement> crawlExcludeTagElements) throws SAXException, IOException,
	        CrawljaxException, XPathExpressionException {
		Document dom = Helper.getDocument(browser.getDom());
		NodeList nodeList = dom.getElementsByTagName(tagElement.getName());
		Set<TagAttribute> attributes = tagElement.getAttributes();

		List<Element> result = new ArrayList<Element>();

		for (int k = 0; k < nodeList.getLength(); k++) {

			Element element = (Element) nodeList.item(k);
			boolean matchesXpath = true;
			EventableCondition eventableCondition =
			        eventableConditionChecker.getEventableCondition(tagElement.getId());
			if (eventableCondition != null && eventableCondition.getInXPath() != null) {
				try {
					matchesXpath =
					        eventableConditionChecker
					                .checkXpathStartsWithXpathEventableCondition(dom,
					                        eventableCondition, XPathHelper
					                                .getXpathExpression(element));
				} catch (Exception e) {
					matchesXpath = false;
					// xpath could not be found or determined, so dont allow
					// element
				}
			}
			// check if element is a candidate
			if (matchesXpath
			        && !crawler.elementIsAlreadyChecked(element.getNodeName() + ": "
			                + Helper.getAllElementAttributes(element))
			        && isElementVisible(dom, browser, element)
			        && !filterElement(attributes, element)) {
				if ("A".equalsIgnoreCase(tagElement.getName())) {
					String href = element.getAttribute("href");
					boolean isExternal = Helper.isLinkExternal(browser.getCurrentUrl(), href);
					boolean isEmail = isEmail(href);
					LOGGER.debug("HREF: " + href + "isExternal= " + isExternal);

					if (!(isExternal || isEmail || isPDForPS(href))) {
						result.add(element);
						crawler.increaseNumberExaminedElements();
					}
				} else {
					result.add(element);
					crawler.increaseNumberExaminedElements();
				}
			}
		}

		if ((crawlExcludeTagElements == null) || (crawlExcludeTagElements.size() == 0)) {
			return result;
		} else {
			List<Element> resultExcluded = new ArrayList<Element>();
			for (Element e : result) {
				if (!isExcluded(dom, e, eventableConditionChecker, crawlExcludeTagElements)) {
					resultExcluded.add(e);
				}
			}

			return resultExcluded;
		}
	}

	/**
	 * @param email
	 *            the string to check
	 * @return true if text has the email pattern.
	 */
	private boolean isEmail(String email) {
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
	private boolean isPDForPS(String href) {
		final Pattern p = Pattern.compile(".+.pdf|.+.ps");
		Matcher m = p.matcher(href);

		if (m.matches()) {
			return true;
		}

		return false;
	}

	/**
	 * @return true if element should be excluded. Also when an ancestor of the given element is
	 *         marked for exclusion, which allows for recursive exclusion of elements from
	 *         candidates.
	 */
	private boolean isExcluded(Document dom, Element element,
	        EventableConditionChecker eventableConditionChecker, List<TagElement> excluded) {

		Node parent = element.getParentNode();

		if (parent instanceof Element) {
			if (isExcluded(dom, (Element) parent, eventableConditionChecker, excluded)) {
				return true;
			}
		}

		for (TagElement tag : excluded) {

			if (element.getTagName().equalsIgnoreCase(tag.getName())) {
				boolean matchesXPath = false;
				EventableCondition eventableCondition =
				        eventableConditionChecker.getEventableCondition(tag.getId());
				try {
					matchesXPath =
					        eventableConditionChecker
					                .checkXpathStartsWithXpathEventableCondition(dom,
					                        eventableCondition, XPathHelper
					                                .getXpathExpression(element));
				} catch (Exception e) {
					// xpath could not be found or determined, so dont filter
					// element because of xpath
					matchesXPath = false;
				}

				if (matchesXPath) {
					LOGGER.info("Excluded element because of xpath: "
					        + new Eventable(element, "").toString());
					return true;
				}
				if (!filterElement(tag.getAttributes(), element)
				        && tag.getAttributes().size() > 0) {
					LOGGER.info("Excluded element because of attributes: "
					        + new Eventable(element, "").toString());
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * @TODO find also whether CSS makes the element invisible!!! --> use WebDriver! Via webdriver
	 *       checking can be very slow
	 */
	private boolean isElementVisible(Document dom, EmbeddedBrowser browser, Element element)
	        throws XPathExpressionException {

		String xpath =
		        XPathHelper.getXpathExpression(element)
		                + "/ancestor::*[contains(@style, 'DISPLAY: none') "
		                + "or contains(@style, 'DISPLAY:none')"
		                + "or contains(@style, 'display: none')"
		                + " or contains(@style, 'display:none')]";

		NodeList nodes = Helper.getElementsByXpath(dom, xpath);

		if (nodes.getLength() > 0) {
			LOGGER
			        .debug("Element: " + Helper.getAllElementAttributes(element)
			                + " is invisible!");

			return false;
		}

		return true;
	}

	/**
	 * Return whether the element is filtered out because of its attributes.
	 */
	private boolean filterElement(Set<TagAttribute> attributes, Element element) {
		int matchCounter = 0;
		if (element == null) {
			return false;
		}
		for (TagAttribute attr : attributes) {
			LOGGER.debug("Checking element " + Helper.getElementString(element)
			        + "AttributeName: " + attr.getName() + " value: " + attr.getValue());

			if (attr.matchesValue(element.getAttribute(attr.getName()))) {
				// make sure that if attribute value is % the element should
				// have this attribute
				if (attr.getValue().equals("%")
				        && element.getAttributeNode(attr.getName()) == null) {
					return true;
				} else {
					matchCounter++;
				}
			} else if (attr.getName().equalsIgnoreCase("innertext")
			        && element.getTextContent() != null) {
				String value = attr.getValue();
				String text = element.getTextContent().trim();
				if (value.contains("%")) {
					String pattern = value.replace("%", "(.*?)");
					if (text.matches(pattern)) {
						matchCounter++;
					}

				} else if (text.equalsIgnoreCase(value)) {
					matchCounter++;
				}
			}

		}

		return (attributes.size() != matchCounter);
	}
}
