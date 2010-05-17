package com.crawljax.condition;

import java.util.concurrent.atomic.AtomicInteger;

import net.jcip.annotations.ThreadSafe;

import org.w3c.dom.NodeList;

import com.crawljax.browser.EmbeddedBrowser;

/**
 * Condition that counts how many times a condition is specified and returns true iff the specified
 * condition is satisfied less than the specified number.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
@ThreadSafe
public class CountCondition implements Condition {

	private final Condition condition;
	private final AtomicInteger count = new AtomicInteger(0);
	private final AtomicInteger maxCount = new AtomicInteger(0);

	/**
	 * @param maxCount
	 *            number of times the condition can be satisfied.
	 * @param condition
	 *            the condition.
	 */
	public CountCondition(int maxCount, Condition condition) {
		this.maxCount.set(maxCount);
		this.condition = condition;
	}

	@Override
	public boolean check(EmbeddedBrowser browser) {
		if (condition.check(browser)) {
			count.getAndIncrement();
		}
		return count.get() <= maxCount.get();
	}

	@Override
	public NodeList getAffectedNodes() {
		return condition.getAffectedNodes();
	}

}
