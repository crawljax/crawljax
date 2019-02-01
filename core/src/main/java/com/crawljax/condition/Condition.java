package com.crawljax.condition;

import com.crawljax.browser.EmbeddedBrowser;
import net.jcip.annotations.ThreadSafe;

/**
 * A condition is a condition which can be tested on the current state in the browser.
 *
 * @author dannyroest@gmail.com (Danny Roest)
 */
@ThreadSafe
public interface Condition {

	/**
	 * @param browser The browser.
	 * @return whether the evaluated condition is satisfied
	 */
	boolean check(EmbeddedBrowser browser);

}
