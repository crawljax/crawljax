package com.crawljax.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.eventablecondition.EventableCondition;
import com.crawljax.condition.eventablecondition.EventableConditionChecker;
import com.crawljax.core.configuration.IgnoreFrameChecker;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.StateVertex;
import com.crawljax.forms.FormHandler;
import com.crawljax.util.Helper;
import com.crawljax.util.XPathHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

/**
 * This class extracts candidate elements from the DOM tree, based on the tags provided by the user.
 * Elements can also be excluded.
 */
public class CandidateElementExtractor {

	private static final Logger LOG = LoggerFactory.getLogger(CandidateElementExtractor.class);

	private final ExtractorManager checkedElements;
	private final EmbeddedBrowser browser;

	private final FormHandler formHandler;
	private final IgnoreFrameChecker ignoreFrameChecker;

	/**
	 * Create a new CandidateElementExtractor.
	 * 
	 * @param checker
	 *            the ExtractorManager to use for marking handled elements and retrieve the
	 *            EventableConditionChecker
	 * @param browser
	 *            the current browser instance used in the Crawler
	 * @param formHandler
	 *            the form handler.
	 * @param ignoreFrameChecker
	 *            the checker used to determine if a certain frame must be ignored.
	 */
	public CandidateElementExtractor(ExtractorManager checker, EmbeddedBrowser browser,
	        FormHandler formHandler, IgnoreFrameChecker ignoreFrameChecker) {
		checkedElements = checker;
		this.browser = browser;
		this.formHandler = formHandler;
		this.ignoreFrameChecker = ignoreFrameChecker;
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
	 * @param currentState
	 *            the state in which this extract method is requested.
	 * @return a list of candidate elements that are not excluded.
	 * @throws CrawljaxException
	 *             if the method fails.
	 */
	public ImmutableList<CandidateElement> extract(List<TagElement> crawlTagElements,
	        List<TagElement> crawlExcludeTagElements, boolean clickOnce, StateVertex currentState)
	        throws CrawljaxException {
		Builder<CandidateElement> results = ImmutableList.builder();

		if (!checkedElements.checkCrawlCondition(browser)) {
			LOG.info("State " + currentState.getName()
			        + " dit not satisfy the CrawlConditions.");
			return results.build();
		}
		LOG.info("Looking in state: {} for candidate elements with ", currentState.getName());

		try {
			Document dom = Helper.getDocument(browser.getDomWithoutIframeContent());
			extractElements(dom, crawlTagElements, crawlExcludeTagElements, clickOnce, results,
			        "");
		} catch (SAXException e) {
			LOG.error(e.getMessage(), e);
			throw new CrawljaxException(e.getMessage(), e);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			throw new CrawljaxException(e.getMessage(), e);
		}
		ImmutableList<CandidateElement> found = results.build();
		LOG.info("Found {} new candidate elements to analyze!", found.size());
		return found;
	}

	private void extractElements(Document dom, List<TagElement> crawlTagElements,
	        List<TagElement> crawlExcludeTagElements, boolean clickOnce,
	        Builder<CandidateElement> results, String relatedFrame) {

		for (TagElement tag : crawlTagElements) {
			LOG.debug("TAG: " + tag.toString());

			List<Element> foundElements =
			        getElementsFromNodelist(dom, crawlExcludeTagElements, tag);

			getFramesCandidates(dom, crawlTagElements, crawlExcludeTagElements, clickOnce,
			        results, relatedFrame);

			getIFramesCandidates(dom, crawlTagElements, crawlExcludeTagElements, clickOnce,
			        results, relatedFrame);

			for (Element sourceElement : foundElements) {
				evaluateElement(clickOnce, results, relatedFrame, tag, sourceElement);
			}
		}
	}

	private List<Element> getElementsFromNodelist(Document dom,
	        List<TagElement> crawlExcludeTagElements, TagElement tag) {
		try {
			return getNodeListForTagElement(dom, tag,
			        checkedElements.getEventableConditionChecker(),
			        crawlExcludeTagElements);
		} catch (XPathExpressionException e) {
			LOG.error("Catched XPathExpression during NodeList For Tag Element retrieval",
			        e);
			return Collections.emptyList();
		} catch (SAXException e) {
			LOG.error("Catched SAXException during NodeList For Tag Element retrieval", e);
			return Collections.emptyList();
		} catch (IOException e) {
			LOG.error("Catched IOException during NodeList For Tag Element retrieval", e);
			return Collections.emptyList();
		}
	}

	private void evaluateElement(boolean clickOnce, Builder<CandidateElement> results,
	        String relatedFrame, TagElement tag, Element sourceElement) {
		EventableCondition eventableCondition =
		        checkedElements.getEventableConditionChecker().getEventableCondition(
		                tag.getId());
		String xpath = XPathHelper.getXPathExpression(sourceElement);
		// get multiple candidate elements when there are input
		// fields connected to this element

		List<CandidateElement> candidateElements = new ArrayList<CandidateElement>();
		if (eventableCondition != null
		        && eventableCondition.getLinkedInputFields() != null
		        && eventableCondition.getLinkedInputFields().size() > 0) {
			// add multiple candidate elements, for every input
			// value combination
			candidateElements =
			        formHandler.getCandidateElementsForInputs(sourceElement,
			                eventableCondition);
		} else {
			// just add default element
			candidateElements.add(new CandidateElement(sourceElement, new Identification(
			        Identification.How.xpath, xpath), relatedFrame));
		}

		for (CandidateElement candidateElement : candidateElements) {
			if (!clickOnce || checkedElements.markChecked(candidateElement)) {
				LOG.info("Found new candidate element: "
				        + candidateElement.getUniqueString());

				if (eventableCondition != null) {
					candidateElement.setEventableCondition(eventableCondition);
				}
				results.add(candidateElement);
				/**
				 * TODO add element to checkedElements after the event is fired! also add string
				 * without 'atusa' attribute to make sure an form action element is only clicked for
				 * its defined values
				 */
			}
		}
	}

	private void getIFramesCandidates(Document dom, List<TagElement> crawlTagElements,
	        List<TagElement> crawlExcludeTagElements, boolean clickOnce,
	        Builder<CandidateElement> results, String relatedFrame) {

		NodeList frameNodes = dom.getElementsByTagName("IFRAME");

		for (int i = 0; frameNodes != null && i < frameNodes.getLength(); i++) {

			String frameIdentification = "";

			if (relatedFrame != null && !relatedFrame.equals("")) {
				frameIdentification += relatedFrame + ".";
			}

			Element frameElement = (Element) frameNodes.item(i);

			String nameId = Helper.getFrameIdentification(frameElement);

			// TODO Stefan; Here the IgnoreFrameChecker is used, also in
			// WebDriverBackedEmbeddedBrowser. We must get this in 1 place.
			if (nameId != null
			        && !ignoreFrameChecker.isFrameIgnored(frameIdentification + nameId)) {
				frameIdentification += nameId;

				LOG.debug("iframe Identification: " + frameIdentification);

				try {
					Document frameDom =
					        Helper.getDocument(browser.getFrameDom(frameIdentification));
					extractElements(frameDom, crawlTagElements, crawlExcludeTagElements,
					        clickOnce, results, frameIdentification);
				} catch (SAXException e) {
					LOG.info("Got exception while inspecting an iframe:" + frameIdentification
					        + " continuing...", e);
				} catch (IOException e) {
					LOG.info("Got exception while inspecting an iframe:" + frameIdentification
					        + " continuing...", e);
				}
			}
		}
	}

	// adds support for FRAME tags
	private void getFramesCandidates(Document dom, List<TagElement> crawlTagElements,
	        List<TagElement> crawlExcludeTagElements, boolean clickOnce,
	        Builder<CandidateElement> results, String relatedFrame) {

		NodeList frameNodes = dom.getElementsByTagName("FRAME");

		for (int i = 0; frameNodes != null && i < frameNodes.getLength(); i++) {

			String frameIdentification = "";

			if (relatedFrame != null && !relatedFrame.equals("")) {
				frameIdentification += relatedFrame + ".";
			}

			Element frameElement = (Element) frameNodes.item(i);

			String nameId = Helper.getFrameIdentification(frameElement);

			// TODO Stefan; Here the IgnoreFrameChecker is used, also in
			// WebDriverBackedEmbeddedBrowser. We must get this in 1 place.
			if (nameId != null
			        && !ignoreFrameChecker.isFrameIgnored(frameIdentification + nameId)) {
				frameIdentification += nameId;

				LOG.debug("frame Identification: " + frameIdentification);

				try {
					Document frameDom =
					        Helper.getDocument(browser.getFrameDom(frameIdentification));
					extractElements(frameDom, crawlTagElements, crawlExcludeTagElements,
					        clickOnce, results, frameIdentification);
				} catch (SAXException e) {
					LOG.info("Got exception while inspecting a frame:" + frameIdentification
					        + " continuing...", e);
				} catch (IOException e) {
					LOG.info("Got exception while inspecting a frame:" + frameIdentification
					        + " continuing...", e);
				}
			}
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
	private List<Element> getNodeListForTagElement(Document dom, TagElement tagElement,
	        EventableConditionChecker eventableConditionChecker,
	        List<TagElement> crawlExcludeTagElements) throws SAXException, IOException,
	        CrawljaxException, XPathExpressionException {

		List<Element> result = new ArrayList<Element>();

		if (tagElement.getName() == null) {
			return result;
		}

		EventableCondition eventableCondition =
		        eventableConditionChecker.getEventableCondition(tagElement.getId());
		// TODO Stefan; this part of the code should be re-factored, Hack-ed it this way to prevent
		// performance problems.
		boolean matchesXpath = true;
		List<String> expressions = null;
		if (eventableCondition != null && eventableCondition.getInXPath() != null) {

			expressions =
			        XPathHelper.getXpathForXPathExpressions(dom, eventableCondition.getInXPath());
		}

		NodeList nodeList = dom.getElementsByTagName(tagElement.getName());
		Set<TagAttribute> attributes = tagElement.getAttributes();

		for (int k = 0; k < nodeList.getLength(); k++) {

			Element element = (Element) nodeList.item(k);
			if (eventableCondition != null && eventableCondition.getInXPath() != null) {
				try {
					matchesXpath =
					        eventableConditionChecker.checkXPathUnderXPaths(
					                XPathHelper.getXPathExpression(element), expressions);
				} catch (Exception e) {
					matchesXpath = false;
					// xpath could not be found or determined, so dont allow
					// element
				}
			}

			// TODO Stefan This is a possible Thread-Interleaving problem, as
			// isChecked can return
			// false and when needed to add it can return true.
			// check if element is a candidate
			if (matchesXpath
			        && !checkedElements.isChecked(element.getNodeName() + ": "
			                + Helper.getAllElementAttributes(element))
			        && isElementVisible(dom, element) && !filterElement(attributes, element)) {
				if ("A".equalsIgnoreCase(tagElement.getName())) {
					String href = element.getAttribute("href");
					boolean isExternal = Helper.isLinkExternal(browser.getCurrentUrl(), href);
					boolean isEmail = isEmail(href);
					LOG.debug("HREF: " + href + "isExternal= " + isExternal);

					if (!(isExternal || isEmail || isPDForPS(href))) {
						result.add(element);
						checkedElements.increaseElementsCounter();
					}
				} else {
					result.add(element);
					checkedElements.increaseElementsCounter();
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

		if (parent instanceof Element
		        && isExcluded(dom, (Element) parent, eventableConditionChecker, excluded)) {
			return true;
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
					                        eventableCondition,
					                        XPathHelper.getXPathExpression(element));
				} catch (Exception e) {
					// xpath could not be found or determined, so dont filter
					// element because of xpath
					matchesXPath = false;
				}

				if (matchesXPath) {
					LOG.info("Excluded element because of xpath: " + element);
					return true;
				}
				if (!filterElement(tag.getAttributes(), element)
				        && tag.getAttributes().size() > 0) {
					LOG.info("Excluded element because of attributes: " + element);
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
	private boolean isElementVisible(Document dom, Element element)
	        throws XPathExpressionException {

		String xpath =
		        XPathHelper.getXPathExpression(element)
		                + "/ancestor::*[contains(@style, 'DISPLAY: none') "
		                + "or contains(@style, 'DISPLAY:none')"
		                + "or contains(@style, 'display: none')"
		                + " or contains(@style, 'display:none')]";

		NodeList nodes = XPathHelper.evaluateXpathExpression(dom, xpath);

		if (nodes.getLength() > 0) {
			LOG.debug("Element: " + Helper.getAllElementAttributes(element) + " is invisible!");

			return false;
		}

		return true;
	}

	/**
	 * Return whether the element is filtered out because of its attributes.
	 */
	private boolean filterElement(Set<TagAttribute> attributes, Element element) {
		int matchCounter = 0;
		if (element == null || attributes == null) {
			return false;
		}
		for (TagAttribute attr : attributes) {
			LOG.debug("Checking element " + Helper.getElementString(element)
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
