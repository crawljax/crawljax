/**
 * 
 */
package com.crawljax.condition.crawlcondition;

import java.util.ArrayList;
import java.util.List;


import org.apache.log4j.Logger;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.Condition;

/**
 * @author danny
 * @version $Id$
 *          Controller class for the crawl conditions
 */
public class CrawlConditionChecker {

	private static final Logger LOGGER = Logger.getLogger(CrawlConditionChecker.class.getName());

	private List<CrawlCondition> crawlConditions;

	private List<CrawlCondition> failedCrawlConditions;

	/**
	 * Constructor.
	 */
	public CrawlConditionChecker() {

	}

	/**
	 * Constructor.
	 * @param crawlConditions Crawlconditions
	 */
	public CrawlConditionChecker(List<CrawlCondition> crawlConditions) {
		this.crawlConditions = crawlConditions;
	}

	/**
	 * @return the crawlConditions
	 */
	public List<CrawlCondition> getCrawlConditions() {
		return crawlConditions;
	}

	/**
	 * @param crawlConditions
	 *            the crawlConditions to set
	 */
	public void setCrawlConditions(List<CrawlCondition> crawlConditions) {
		this.crawlConditions = crawlConditions;
	}

	/**
	 * @param browser The browser.
	 * @return true iff there are no conditions to test OR the browser satisfies
	 *         ONE OR MORE of the crawlConditions
	 */
	public boolean check(EmbeddedBrowser browser) {

		failedCrawlConditions = new ArrayList<CrawlCondition>();

		if (crawlConditions != null) {
			for (CrawlCondition crawlCondition : crawlConditions) {
				boolean conditionsSucceed = true;
				for (Condition condition : crawlCondition.getPreConditions()) {
					boolean check;
					check = condition.check(browser);
					if (!check) {
						conditionsSucceed = false;
						break;
					}
				}
				if (conditionsSucceed) {
					Condition condition = crawlCondition.getCondition();
					// LOGGER.info("Checking Crawl Condition "
					// + crawlCondition.getClass().getSimpleName() + ": "
					// + crawlCondition.getDescription());
					if (!condition.check(browser)) {
						LOGGER.info("CrawlCondition failed: " + crawlCondition.getDescription());
						failedCrawlConditions.add(crawlCondition);
						return false;
					}
				}
			}
		}

		return true;
	}

	/**
	 * @return the failedCrawlCondition
	 */
	public List<CrawlCondition> getFailedCrawlConditions() {
		return failedCrawlConditions;
	}

}
