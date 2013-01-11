/**
 * A plugin interface to provide an extension point for comparing the current state with the new
 * state induced after firing the event.
 * 
 * Note that if you add more than one instance of this type of plugin to crawljax, only the last
 * added instance will be used for performing the DOM comparison and all others will be ignored.
 * 
 */
package com.crawljax.core.plugin;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.state.Eventable;

public interface DomChangeNotifierPlugin extends Plugin {

	/**
	 * Check to see if the (new) DOM is changed with regards to the old DOM.
	 * 
	 * @param stateBefore
	 *            the state before the event.
	 * @param stateAfter
	 *            the state after the event.
	 * @return true if the state is changed according to the compare method of the oracle.
	 */
	boolean isDomChanged(final String domBefore, final Eventable e, final String domAfter,
	        EmbeddedBrowser browser);

}
