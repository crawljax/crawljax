package com.crawljax.condition;

import java.util.concurrent.atomic.AtomicInteger;

import net.jcip.annotations.ThreadSafe;

import org.w3c.dom.NodeList;

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

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
		        .add("super", super.toString())
		        .add("condition", condition)
		        .add("count", count)
		        .add("maxCount", maxCount)
		        .toString();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), condition, count, maxCount);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof CountCondition) {
			if (!super.equals(object))
				return false;
			CountCondition that = (CountCondition) object;
			return Objects.equal(this.condition, that.condition)
			        && Objects.equal(this.count, that.count)
			        && Objects.equal(this.maxCount, that.maxCount);
		}
		return false;
	}

}
