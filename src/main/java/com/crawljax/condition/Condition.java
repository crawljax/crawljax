/**
 * 
 */
package com.crawljax.condition;

import net.jcip.annotations.ThreadSafe;

import org.w3c.dom.NodeList;

import com.crawljax.browser.EmbeddedBrowser;

/**
 * A condition is a condition which can be tested on the current state in the browser.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
@ThreadSafe
public interface Condition {

	/**
	 * @param browser
	 *            The browser.
	 * @return whether the evaluated condition is satisfied
	 */
	boolean check(EmbeddedBrowser browser);

	/**
	 * @return the affected nodes (can be null)
	 */
	NodeList getAffectedNodes();
}
