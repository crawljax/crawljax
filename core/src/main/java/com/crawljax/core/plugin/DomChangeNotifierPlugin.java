/**
 * A plugin interface to provide an extension point for comparing the current state with the new
 * state induced after firing the event. Note that if you add more than one instance of this type of
 * plugin to crawljax, only the last added instance will be used for performing the DOM comparison
 * and all others will be ignored.
 */
package com.crawljax.core.plugin;

import com.crawljax.core.CrawlerContext;
import com.crawljax.core.state.Eventable;

/**
 * This plugins lets you override the default state comparison that Crawljax uses.
 *
 * @deprecated Allthough new states are selected based on this plugin, the actual state comparison used by the
 * backing StateFlowGraph is uses the {@link Object#hashCode()} and {@link Object#equals(Object)} function of the
 * {@link com.crawljax.core.state.StateVertex}. To implement correct behaviour, do note use this class but specify a
 * custom {@link com.crawljax.core.state.StateVertexFactory} in the
 * {@link com.crawljax.core.configuration.CrawljaxConfiguration}. This method will be removed in Crawljax 4.x
 */
@Deprecated
public interface DomChangeNotifierPlugin extends Plugin {

	/**
	 * Check to see if the (new) DOM is changed with regards to the old DOM.
	 * <p>
	 * This method can be called from multiple threads with different {@link CrawlerContext}
	 * </p>
	 *
	 * @param context   The Crawler context.
	 * @param domBefore the state before the event.
	 * @param domAfter  the state after the event.
	 * @return true if the state is changed according to the compare method of the oracle.
	 * @deprecated See class documentation. This method will be removed in Crawljax 4.x
	 */
	@Deprecated
	boolean isDomChanged(CrawlerContext context, String domBefore, Eventable e, String domAfter);

}
