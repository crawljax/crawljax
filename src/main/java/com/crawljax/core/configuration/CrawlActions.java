package com.crawljax.core.configuration;

import java.util.ArrayList;
import java.util.List;

import com.crawljax.condition.Condition;
import com.crawljax.core.state.Eventable.EventType;

/**
 * Specifies the actions for CrawlElements NOTE: In general CrawlActions is not designed to be
 * instantiated directly. CrawlActions should be used via {@link CrawlSpecification} To add
 * conditions to check whether a tag should (not) be clicked one can use {@link #when(Condition...)}
 * . See also {@link Condition}
 * 
 * @author DannyRoest@gmail.com (Danny Roest)
 * @version $Id$
 */
public class CrawlActions {

	private final List<CrawlElement> crawlElements = new ArrayList<CrawlElement>();
	private final List<CrawlElement> crawlElementsExcluded = new ArrayList<CrawlElement>();
	private Condition[] tempConditions;

	/**
	 * Protected Constructor of CrawlActions can only be instanced from class from the same package.
	 */
	protected CrawlActions() {

	}

	/**
	 * Set of HTML elements Crawljax will click during crawling For exmple 1) <a.../> 2) <div/>
	 * click("a") will only include 1 if clickOnce is true (default). This set can be restricted by
	 * {@link #dontClick(String)}.
	 * 
	 * @param tagName
	 *            the tag name of the elements to be included
	 * @return this CrawlElement
	 */
	public CrawlElement click(String tagName) {
		CrawlElement crawlTag = new CrawlElement(EventType.click);
		crawlTag.setTagName(tagName);
		setTempConditions(crawlTag);
		crawlElements.add(crawlTag);
		return crawlTag;
	}

	/**
	 * Set of HTML elements Crawljax will NOT click during crawling When an HTML is present in the
	 * click and dontClick sets, then the element will not be clicked. For example: 1) <a
	 * href="#">Some text</a> 2) <a class="foo" .../> 3) <div class="foo" .../> click("a")
	 * dontClick("a").withAttribute("class", "foo"); Will include only include HTML element 2
	 * 
	 * @param tagName
	 *            the tag name of the elements to be excluded
	 * @return crawlTag the CrawlElement
	 */
	public CrawlElement dontClick(String tagName) {
		CrawlElement crawlTag = new CrawlElement(EventType.click);
		crawlTag.setTagName(tagName);
		setTempConditions(crawlTag);
		crawlElementsExcluded.add(crawlTag);
		return crawlTag;
	}

	/**
	 * Crawljax will crawl the HTML elements while crawling if and only if all the specified
	 * conditions are satisfied. IMPORTANT: only works with click()!!! For example:
	 * when(onContactPageCondition) will only click the HTML element if it is on the contact page
	 * 
	 * @param conditions
	 *            the condition to be met.
	 * @return this
	 */
	public CrawlActions when(Condition... conditions) {
		tempConditions = conditions;
		return this;
	}

	/* private and protected */

	/**
	 * @param crawlTag
	 */
	private void setTempConditions(CrawlElement crawlTag) {
		if (tempConditions != null) {
			crawlTag.setConditions(tempConditions);
			tempConditions = null;
		}
	}

	/**
	 * @return the crawlElements
	 */
	protected List<CrawlElement> getCrawlElements() {
		return crawlElements;
	}

	/**
	 * @return the crawlElementsExcluded
	 */
	protected List<CrawlElement> getCrawlElementsExcluded() {
		return crawlElementsExcluded;
	}

}
