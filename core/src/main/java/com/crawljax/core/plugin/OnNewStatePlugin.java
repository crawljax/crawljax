package com.crawljax.core.plugin;

import com.crawljax.core.CrawlerContext;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertex;

/**
 * Plugin type that is called every time a new state is found by Crawljax. This also happens for the
 * Index State. Example: DOM validation.
 */
public interface OnNewStatePlugin extends Plugin {

	/**
	 * Method that is called when a new state is found. When this method is called the state is
	 * already added to the {@link StateFlowGraph}.
	 * <p>
	 * This method can be called from multiple threads with different {@link CrawlerContext}
	 * </p>
	 * 
	 * @param context
	 *            the current context.
	 * @param newState
	 *            The new state. Equivalent to {@link CrawlerContext#getCurrentState()}.
	 */
	void onNewState(CrawlerContext context, StateVertex newState);

}
