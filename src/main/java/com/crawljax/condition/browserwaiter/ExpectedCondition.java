package com.crawljax.condition.browserwaiter;

import com.crawljax.browser.EmbeddedBrowser;

/**
 * Interface for defining conditions to wait for.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
public interface ExpectedCondition {

	/**
	 * 
	 * @return Whether the condition is statisfied. 
	 */
	boolean isSatisfied();

	/**
	 * Returns the browser.
	 * @return The browser.
	 */
	EmbeddedBrowser getBrowser();

	/**
	 * Sets the browser.
	 * @param browser The browser.
	 */
	void setBrowser(EmbeddedBrowser browser);

}
