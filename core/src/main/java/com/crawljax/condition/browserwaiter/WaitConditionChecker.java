package com.crawljax.condition.browserwaiter;

import java.util.List;

import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;

/**
 * Checks the wait conditions.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
@ThreadSafe
public class WaitConditionChecker {

	private static final Logger LOGGER = LoggerFactory.getLogger(WaitConditionChecker.class
	        .getName());

	private List<WaitCondition> waitConditions;

	/**
	 * Default constructor.
	 */
	public WaitConditionChecker() {

	}

	/**
	 * Constructor with wait conditions.
	 * 
	 * @param waitConditions
	 *            The wait conditions.
	 */
	public WaitConditionChecker(List<WaitCondition> waitConditions) {
		this.waitConditions = waitConditions;
	}

	/**
	 * @return the waitConditions
	 */
	public List<WaitCondition> getWaitConditions() {
		return waitConditions;
	}

	/**
	 * @param waitConditions
	 *            the waitConditions to set
	 */
	public void setWaitConditions(List<WaitCondition> waitConditions) {
		this.waitConditions = waitConditions;
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
