package com.crawljax.core.plugin;

import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.CrawlSession;

/**
 * Plugin type that is called every time an invariant is violated. Invariants are checked after each
 * detected state change.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
public interface OnInvariantViolationPlugin extends Plugin {

	/**
	 * Method that is called when an invariant is violated. Warning: changing the session can change
	 * the behavior of Crawljax. It is not a copy!
	 * 
	 * @param invariant
	 *            the failed invariant.
	 * @param session
	 *            the current session.
	 */
	void onInvariantViolation(Invariant invariant, CrawlSession session);

}
