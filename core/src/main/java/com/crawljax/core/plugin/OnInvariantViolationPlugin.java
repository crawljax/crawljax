package com.crawljax.core.plugin;

import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.CrawlerContext;

/**
 * Plugin type that is called every time an invariant is violated. Invariants are checked after each
 * detected state change.
 */
public interface OnInvariantViolationPlugin extends Plugin {

	/**
	 * Method that is called when an invariant is violated.
	 * <p>
	 * This method can be called from multiple threads with different {@link CrawlerContext}
	 * </p>
	 * 
	 * @param invariant
	 *            the failed invariant.
	 * @param context
	 *            the browsers context
	 */
	void onInvariantViolation(Invariant invariant, CrawlerContext context);

}
