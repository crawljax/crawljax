package com.crawljax.core;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.eventablecondition.EventableCondition;
import com.crawljax.condition.eventablecondition.EventableConditionChecker;
import com.crawljax.core.configuration.CrawlElement;
import com.crawljax.core.configuration.CrawlRules;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.PreCrawlConfiguration;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.StateVertex;
import com.crawljax.forms.FormHandler;
import com.crawljax.util.DomUtils;
import com.crawljax.util.XPathHelper;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.inject.assistedinject.Assisted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.inject.Inject;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class extracts candidate elements from the DOM tree, based on the tags provided by the user.
 * Elements can also be excluded.
 */
public class CandidateElementExtractor {

	private static final Logger LOG = LoggerFactory.getLogger(CandidateElementExtractor.class);

	private final ExtractorManager checkedElements;
	private final EmbeddedBrowser browser;

	private final FormHandler formHandler;
	private final boolean crawlFrames;
	private final ImmutableMultimap<String, CrawlElement> excludeCrawlElements;
	private final ImmutableList<CrawlElement> includedCrawlElements;

	private final boolean clickOnce;
	private final boolean randomizeElementsOrder;

	private final ImmutableSortedSet<String> ignoredFrameIdentifiers;

	private final boolean followExternalLinks;

	private final String siteHostName;

	/**
	 * Create a new CandidateElementExtractor.
	 *
	 * @param checker     the ExtractorManager to use for marking handled elements and retrieve the
	 *                    EventableConditionChecker
	 * @param browser     the current browser instance used in the Crawler
	 * @param formHandler the form handler.
	 * @param config      the checker used to determine if a certain frame must be ignored.
	 */
	@Inject
	public CandidateElementExtractor(ExtractorManager checker, @Assisted EmbeddedBrowser browser,
			FormHandler formHandler, CrawljaxConfiguration config) {
		checkedElements = checker;
		this.browser = browser;
		this.formHandler = formHandler;
		CrawlRules rules = config.getCrawlRules();
		PreCrawlConfiguration preCrawlConfig = rules.getPreCrawlConfig();
		this.excludeCrawlElements = asMultiMap(preCrawlConfig.getExcludedElements());
		this.includedCrawlElements = ImmutableList.<CrawlElement>builder()
				.addAll(preCrawlConfig.getIncludedElements())
				.addAll(rules.getInputSpecification().getCrawlElements())
				.build();
		crawlFrames = rules.shouldCrawlFrames();
		clickOnce = rules.isClickOnce();
		randomizeElementsOrder = rules.isRandomizeCandidateElements();
		ignoredFrameIdentifiers = rules.getIgnoredFrameIdentifiers();
		followExternalLinks = rules.followExternalLinks();
		siteHostName = config.getUrl().getHost();
	}

	private ImmutableMultimap<String, CrawlElement> asMultiMap(
			ImmutableList<CrawlElement> elements) {
		ImmutableMultimap.Builder<String, CrawlElement> builder = ImmutableMultimap.builder();
		for (CrawlElement elem : elements) {
			builder.put(elem.getTagName(), elem);
		}
		return builder.build();
	}

	/**
	 * This method extracts candidate elements from the current DOM tree in the browser, based on
	 * the crawl tags defined by the user.
	 *
	 * @param currentState the state in which this extract method is requested.
	 * @return a list of candidate elements that are not excluded.
	 * @throws CrawljaxException if the method fails.
	 */
	public ImmutableList<CandidateElement> extract(StateVertex currentState)
			throws CrawljaxException {
		LinkedList<CandidateElement> results = new LinkedList<>();

		if (!checkedElements.checkCrawlCondition(browser)) {
			LOG.info("State {} did not satisfy the CrawlConditions.", currentState.getName());
			return ImmutableList.of();
		}
		LOG.debug("Looking in state: {} for candidate elements", currentState.getName());

		try {
//			Document dom = DomUtils.asDocument(browser.getStrippedDomWithoutIframeContent());
			Document dom = currentState.getDocument();
			extractElements(dom, results, "");
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			throw new CrawljaxException(e);
		}
		if (randomizeElementsOrder) {
			Collections.shuffle(results);
		}
		currentState.setElementsFound(results);
		LOG.debug("Found {} new candidate elements to analyze!", results.size());
		return ImmutableList.copyOf(results);
	}

	private void extractElements(Document dom, List<CandidateElement> results,
			String relatedFrame) {
		LOG.debug("Extracting elements for related frame '{}'", relatedFrame);
		for (CrawlElement tag : includedCrawlElements) {
			LOG.debug("Extracting TAG: {}", tag);

			NodeList frameNodes = dom.getElementsByTagName("FRAME");
			addFramesCandidates(dom, results, relatedFrame, frameNodes);

			NodeList iFrameNodes = dom.getElementsByTagName("IFRAME");
			addFramesCandidates(dom, results, relatedFrame, iFrameNodes);

			evaluateElements(dom, tag, results, relatedFrame);
		}
	}

	private void addFramesCandidates(Document dom, List<CandidateElement> results,
			String relatedFrame, NodeList frameNodes) {

		if (frameNodes == null) {
			return;
		}

		for (int i = 0; i < frameNodes.getLength(); i++) {

			Element frameElement = (Element) frameNodes.item(i);

			String nameId = DomUtils.getFrameIdentification(frameElement);

			String frameIdentification = "";
			if (!Strings.isNullOrEmpty(relatedFrame)) {
				frameIdentification += relatedFrame + ".";
			}
			// TODO Stefan; Here the IgnoreFrameChecker is used, also in
			// WebDriverBackedEmbeddedBrowser. We must get this in 1 place.
			if (nameId == null || isFrameIgnored(frameIdentification + nameId)) {
				continue;
			} else {
				frameIdentification += nameId;

				LOG.debug("frame Identification: {}", frameIdentification);

				try {
					Document frameDom =
							DomUtils.asDocument(browser.getFrameDom(frameIdentification));
					extractElements(frameDom, results, frameIdentification);
				} catch (IOException e) {
					LOG.info("Got exception while inspecting a frame: {} continuing...",
							frameIdentification, e);
				}
			}
		}
	}

	private boolean isFrameIgnored(String string) {
		if (crawlFrames) {
			for (String ignorePattern : ignoredFrameIdentifiers) {
				if (ignorePattern.contains("%")) {
					// replace with a useful wildcard for regex
					String pattern = ignorePattern.replace("%", ".*");
					if (string.matches(pattern)) {
						return true;
					}
				} else if (ignorePattern.equals(string)) {
					return true;
				}
			}
			return false;
		} else {
			return true;
		}
	}

	private void evaluateElements(Document dom, CrawlElement crawl,
			List<CandidateElement> results, String relatedFrame) {
		try {
			List<Element> nodeListForCrawlElement =
					getNodeListForTagElement(dom, crawl,
							checkedElements.getEventableConditionChecker());

			for (Element sourceElement : nodeListForCrawlElement) {
				evaluateElement(results, relatedFrame, crawl, sourceElement);
			}
		} catch (CrawljaxException e) {
			LOG.warn("Caught exception during NodeList For Tag Element retrieval", e);
		}
	}

	/**
	 * Returns a list of Elements form the DOM tree, matching the tag element.
	 */
	private ImmutableList<Element> getNodeListForTagElement(Document dom,
			CrawlElement crawlElement,
			EventableConditionChecker eventableConditionChecker) {

		Builder<Element> result = ImmutableList.builder();

		if (crawlElement.getTagName() == null) {
			return result.build();
		}

		EventableCondition eventableCondition =
				eventableConditionChecker.getEventableCondition(crawlElement.getId());
		// TODO Stefan; this part of the code should be re-factored, Hack-ed it this way to prevent
		// performance problems.
		ImmutableList<String> expressions = getFullXpathForGivenXpath(dom, eventableCondition);

		NodeList nodeList = dom.getElementsByTagName(crawlElement.getTagName());

		for (int k = 0; k < nodeList.getLength(); k++) {

			Element element = (Element) nodeList.item(k);
			boolean matchesXpath =
					elementMatchesXpath(eventableConditionChecker, eventableCondition,
							expressions, element);
			LOG.debug("Element {} matches Xpath={}", DomUtils.getElementString(element),
					matchesXpath);
			/*
			 * TODO Stefan This is a possible Thread-Interleaving problem, as / isChecked can return
			 * false and when needed to add it can return true. / check if element is a candidate
			 */
			String id = element.getNodeName() + ": " + DomUtils.getAllElementAttributes(element);
			if (matchesXpath && !checkedElements.isChecked(id)
					&& !isExcluded(dom, element, eventableConditionChecker)) {
				addElement(element, result, crawlElement);
			} else {
				LOG.debug("Element {} was not added", element);
			}
		}
		return result.build();
	}

	private boolean elementMatchesXpath(EventableConditionChecker eventableConditionChecker,
			EventableCondition eventableCondition, ImmutableList<String> expressions,
			Element element) {
		boolean matchesXpath = true;
		if (eventableCondition != null && eventableCondition.getInXPath() != null) {
			try {
				matchesXpath =
						eventableConditionChecker.checkXPathUnderXPaths(
								XPathHelper.getXPathExpression(element), expressions);
			} catch (RuntimeException e) {
				matchesXpath = false;
			}
		}
		return matchesXpath;
	}

	private ImmutableList<String> getFullXpathForGivenXpath(Document dom,
			EventableCondition eventableCondition) {
		if (eventableCondition != null && eventableCondition.getInXPath() != null) {
			try {
				ImmutableList<String> result =
						XPathHelper.getXpathForXPathExpressions(dom,
								eventableCondition.getInXPath());
				LOG.debug("Xpath {} resolved to XPaths in document: {}",
						eventableCondition.getInXPath(), result);
				return result;
			} catch (XPathExpressionException e) {
				LOG.debug("Could not load XPath expressions for {}", eventableCondition, e);
			}
		}
		return ImmutableList.of();
	}

	private void addElement(Element element, Builder<Element> builder,
			CrawlElement crawlElement) {
		if ("A".equalsIgnoreCase(crawlElement.getTagName()) && hrefShouldBeIgnored(element)) {
			return;
		}
		builder.add(element);
		LOG.debug("Adding element {}", element);
		checkedElements.increaseElementsCounter();
	}

	private boolean hrefShouldBeIgnored(Element element) {
		String href = Strings.nullToEmpty(element.getAttribute("href"));
		return isFileForDownloading(href)
				|| href.startsWith("mailto:")
				|| (!followExternalLinks && isExternal(href));
	}

	private boolean isExternal(String href) {
		if (href.startsWith("http")) {
			try {
				URI uri = URI.create(href);
				return !uri.getHost().equalsIgnoreCase(siteHostName);
			} catch (IllegalArgumentException e) {
				LOG.info("Unreadable external link {}", href);
			}
		}
		return false;
	}

	/**
	 * @param href the string to check
	 * @return true if href has the pdf or ps pattern.
	 */
	private boolean isFileForDownloading(String href) {
		final Pattern p = Pattern.compile(".+.pdf|.+.ps|.+.zip|.+.mp3");
		return p.matcher(href).matches();
	}

	private void evaluateElement(List<CandidateElement> results, String relatedFrame,
			CrawlElement crawl, Element sourceElement) {
		EventableCondition eventableCondition =
				checkedElements.getEventableConditionChecker().getEventableCondition(
						crawl.getId());
		String xpath = XPathHelper.getXPathExpression(sourceElement);
		// get multiple candidate elements when there are input
		// fields connected to this element

		List<CandidateElement> candidateElements = new ArrayList<>();
		if (eventableCondition != null && eventableCondition.getLinkedInputFields() != null
				&& eventableCondition.getLinkedInputFields().size() > 0) {
			// add multiple candidate elements, for every input
			// value combination
			candidateElements =
					formHandler.getCandidateElementsForInputs(sourceElement, eventableCondition);
		} else {
			// just add default element
			candidateElements.add(new CandidateElement(sourceElement, new Identification(
					Identification.How.xpath, xpath), relatedFrame));
		}

		for (CandidateElement candidateElement : candidateElements) {
			if (!clickOnce || checkedElements.markChecked(candidateElement)) {
				LOG.debug("Found new candidate element: {} with eventableCondition {}",
						candidateElement.getUniqueString(), eventableCondition);
				candidateElement.setEventableCondition(eventableCondition);
				// Setting eventType so that correct event can be set for crawlAction
				candidateElement.setEventType(crawl.getEventType());
				results.add(candidateElement);
				/*
				 * TODO add element to checkedElements after the event is fired! also add string
				 * without 'atusa' attribute to make sure an form action element is only clicked for
				 * its defined values
				 */
			}
		}
	}

	/**
	 * @return true if element should be excluded. Also when an ancestor of the given element is
	 * marked for exclusion, which allows for recursive exclusion of elements from
	 * candidates.
	 */
	private boolean isExcluded(Document dom, Element element,
			EventableConditionChecker eventableConditionChecker) {

		Node parent = element.getParentNode();

		if (parent instanceof Element
				&& isExcluded(dom, (Element) parent, eventableConditionChecker)) {
			return true;
		}

		for (CrawlElement crawlElem : excludeCrawlElements
				.get(element.getTagName().toUpperCase())) {
			boolean matchesXPath = false;
			EventableCondition eventableCondition =
					eventableConditionChecker.getEventableCondition(crawlElem.getId());
			try {
				String asXpath = XPathHelper.getXPathExpression(element);
				matchesXPath =
						eventableConditionChecker.checkXpathStartsWithXpathEventableCondition(
								dom, eventableCondition, asXpath);
			} catch (CrawljaxException | XPathExpressionException e) {
				LOG.debug("Could not check exclusion by Xpath for element because {}",
						e.getMessage());
				matchesXPath = false;
			}

			if (matchesXPath) {
				LOG.info("Excluded element because of xpath: " + element);
				return true;
			}
		}

		return false;
	}

	public boolean checkCrawlCondition() {
		return checkedElements.checkCrawlCondition(browser);
	}
}
