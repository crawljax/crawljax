package com.crawljax.util;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.state.Element;
import com.crawljax.core.state.Eventable;

/**
 * Finds and checks elements.
 */
public class ElementResolver {
	private static final Logger LOGGER = LoggerFactory.getLogger(ElementResolver.class);

	private final EmbeddedBrowser browser;
	private final Eventable eventable;

	/**
	 * Constructor.
	 * 
	 * @param eventable
	 *            Eventable.
	 * @param browser
	 *            The browser.
	 */
	public ElementResolver(Eventable eventable, EmbeddedBrowser browser) {
		this.browser = browser;
		this.eventable = eventable;
	}

	/**
	 * @return equivalent xpath of element equivalent to Eventable
	 */
	public String resolve() {
		return resolve(false);
	}

	/**
	 * @param logging
	 *            Whether to do logging.
	 * @return equivalent xpath of element equivalent to Eventable or an empty string if the DOM
	 *         cannot be read.
	 */
	public String resolve(boolean logging) {
		Document dom = null;
		try {
			dom = DomUtils.asDocument(browser.getStrippedDom());
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			return "";
		}

		try {
			String xpathEventable = eventable.getIdentification().getValue();
			Node nodeSameXpath = DomUtils.getElementByXpath(dom, xpathEventable);
			if (nodeSameXpath != null) {
				Element elementSameXpath = new Element(nodeSameXpath);
				if (logging) {
					LOGGER.info("Try element with same xpath expression");
				}
				if (equivalent(elementSameXpath, logging)) {
					return xpathEventable;
				}
			}

			if (logging) {
				LOGGER.info("Search other candidate elements");
			}
			NodeList candidateElements =
			        XPathHelper.evaluateXpathExpression(dom, "//"
			                + eventable.getElement().getTag().toUpperCase());
			if (logging) {
				LOGGER.info("Candidates: " + candidateElements.getLength());
			}
			for (int i = 0; i < candidateElements.getLength(); i++) {
				Element candidateElement = new Element(candidateElements.item(i));
				if (equivalent(candidateElement, logging)) {
					return XPathHelper.getXPathExpression(candidateElements.item(i));
				}
			}

		} catch (XPathExpressionException e) {
			LOGGER.error(e.getMessage(), e);
		}
		if (logging) {
			LOGGER.info("No equivalent element found");
		}
		return null;
	}

	/**
	 * Comparator against other element.
	 * 
	 * @param otherElement
	 *            The other element.
	 * @param logging
	 *            Whether to do logging.
	 * @return Whether the elements are equal.
	 */
	public boolean equivalent(Element otherElement, boolean logging) {
		if (eventable.getElement().equals(otherElement)) {
			if (logging) {
				LOGGER.info("Element equal");
			}
			return true;
		}

		if (eventable.getElement().equalAttributes(otherElement)) {
			if (logging) {
				LOGGER.info("Element attributes equal");
			}
			return true;
		}

		if (eventable.getElement().equalId(otherElement)) {
			if (logging) {
				LOGGER.info("Element ID equal");
			}
			return true;
		}

		if (!eventable.getElement().getText().equals("")
		        && eventable.getElement().equalText(otherElement)) {

			if (logging) {
				LOGGER.info("Element text equal");
			}

			return true;
		}

		return false;
	}

}
