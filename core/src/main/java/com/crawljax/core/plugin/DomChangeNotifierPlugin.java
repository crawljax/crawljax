/**
 * A plug-in interface to provide an extension point for comparing current state
 *  with the state induced by firing events on clickables. 
 * 
 * @author alireza.aut@gmail.com, Alireza Zarei
 *
 *
 *I
 */
package com.crawljax.core.plugin;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.state.Eventable;

public interface DomChangeNotifierPlugin extends Plugin {



	/**
	 * Test to see if the (new) DOM is changed with regards to the old DOM. 
	 * 
	 * @param stateBefore
	 *            the state before the event.
	 * @param stateAfter
	 *            the state after the event.
	 * @return true if the state is changed according to the compare method of the oracle.
	 */
	 boolean isDomChanged(final String domBefore,final Eventable e, final String domAfter, EmbeddedBrowser browser);
	 
	 
	 
	 
}
