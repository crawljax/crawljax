package com.crawljax.condition.browserwaiter;

import java.util.List;

import javax.inject.Inject;

import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.configuration.CrawlRules;
import com.google.common.collect.ImmutableList;

/**
 * Checks the wait conditions.
 */
@ThreadSafe
public class WaitConditionChecker {

	private static final Logger LOGGER = LoggerFactory.getLogger(WaitConditionChecker.class
	        .getName());

	private ImmutableList<WaitCondition> waitConditions;

	@Inject
	public WaitConditionChecker(CrawlRules rules) {
		waitConditions = rules.getPreCrawlConfig().getWaitConditions();
	}

	/**
	 * @return the waitConditions
	 */
	public List<WaitCondition> getWaitConditions() {
		return waitConditions;
	}

	/**
	 * @param browser
	 *            The browser to use.
	 */
	public void wait(EmbeddedBrowser browser) {
		if (waitConditions == null) {
			return;
		}
		for (WaitCondition waitCondition : waitConditions) {
			LOGGER.info("Checking WaitCondition for url: " + waitCondition.getUrl());
			waitCondition.testAndWait(browser);
		}
	}

}
