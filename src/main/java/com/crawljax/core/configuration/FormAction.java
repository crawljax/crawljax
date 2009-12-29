package com.crawljax.core.configuration;

/**
 * Represents a form action, e.g. a link that is clicked that handles the form NOTE: In general
 * FormAction is not designed to be instantiated directly.
 * 
 * @author DannyRoest@gmail.com (Danny Roest)
 */
public class FormAction {

	private CrawlElement crawlElement;

	/**
	 * @param tagName
	 *            the tag name of the element
	 * @return this CrawlElement
	 */
	public CrawlElement beforeClickElement(String tagName) {
		this.crawlElement = new CrawlElement();
		this.crawlElement.setTagName(tagName);
		return crawlElement;
	}

	/**
	 * @return the crawlTag
	 */
	protected CrawlElement getCrawlElement() {
		return crawlElement;
	}

}
