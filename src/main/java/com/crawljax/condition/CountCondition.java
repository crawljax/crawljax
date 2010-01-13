package com.crawljax.condition;

import org.w3c.dom.NodeList;

import com.crawljax.browser.EmbeddedBrowser;

/**
 * Condition that counts how many times a condition is specified and returns true iff the specified
 * condition is satisfied less than the specified number.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
public class CountCondition implements Condition {

	private Condition condition;
	private int count = 0;
	private int maxCount = 0;

	/**
	 * @param maxCount
	 *            number of times the condition can be satisfied.
	 * @param condition
	 *            the condition.
	 */
	public CountCondition(int maxCount, Condition condition) {
		this.maxCount = maxCount;
		this.condition = condition;
	}

	@Override
	public boolean check(EmbeddedBrowser browser) {
		if (condition.check(browser)) {
			count++;
		}
		return count <= maxCount;
	}

	@Override
	public NodeList getAffectedNodes() {
		return condition.getAffectedNodes();
	}

}
