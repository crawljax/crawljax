package com.crawljax.condition;

import java.util.concurrent.atomic.AtomicInteger;

import net.jcip.annotations.ThreadSafe;

import com.crawljax.browser.EmbeddedBrowser;
import com.google.common.base.Objects;

/**
 * Condition that counts how many times a condition is specified and returns true iff the specified
 * condition is satisfied less than the specified number.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 */
@ThreadSafe
public class CountCondition implements Condition {

	private final Condition condition;
	private final AtomicInteger count = new AtomicInteger(0);
	private final int maxCount;

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

	/**
	 * Note: Check has a side effect (it increments a counter). Invoking it multiple times may
	 * result in a different answer.
	 */
	@Override
	public boolean check(EmbeddedBrowser browser) {
		if (condition.check(browser)) {
			count.getAndIncrement();
		}
		return count.get() <= maxCount;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
		        .add("condition", condition)
		        .add("maxCount", maxCount)
		        .toString();
	}

	/**
	 * Since "count" is a consequence of invoking "check", it is not included in the equality /
	 * hashCode computation.
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(getClass(), condition, maxCount);
	}

	/**
	 * Since "count" is a consequence of invoking "check", it is not included in the equality /
	 * hashCode computation.
	 */
	@Override
	public boolean equals(Object object) {
		if (object instanceof CountCondition) {
			CountCondition that = (CountCondition) object;
			return Objects.equal(this.condition, that.condition)
			        && Objects.equal(this.maxCount, that.maxCount);
		}
		return false;
	}

}
