package com.crawljax.core.plugin;

import com.crawljax.core.CrawlerContext;
import com.crawljax.core.state.StateVertex;

/**
 * Plugin type that is called every time a new state is found by Crawljax. This also happens for the
 * Index State. Example: DOM validation.
 */
public interface OnNewStatePlugin extends Plugin {

	/**
	 * Method that is called when a new state is found. Warning: changing the session can change the
	 * behavior of Crawljax. It is not a copy! *
	 * <p>
	 * This method can be called from multiple threads with different {@link CrawlerContext}
	 * </p>
	 * 
	 * @param context
	 *            the current context.
	 * @param newState
	 *            The new state
	 */
	void onNewState(CrawlerContext context, StateVertex newState);

}
