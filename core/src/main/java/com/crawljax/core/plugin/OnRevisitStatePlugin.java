package com.crawljax.core.plugin;

import com.crawljax.core.CrawlerContext;
import com.crawljax.core.state.StateVertex;

/**
 * Plugin type that is called every time a state is revisited by Crawljax. Example: Benchmarking.
 * This plugin needs an explicit current state because the session.getCurrentState() does not
 * contain the correct current state since we are in back-tracking phase.
 */
public interface OnRevisitStatePlugin extends Plugin {

	/**
	 * Method that is called every time a state is revisited by Crawljax. Warning: changing the
	 * state can influence crawljax, it is not a copy.
	 * <p>
	 * This method can be called from multiple threads with different {@link CrawlerContext}
	 * </p>
	 * 
	 * @param context
	 *            the crawlSession
	 * @param currentState
	 *            the state revisited
	 */
	void onRevisitState(CrawlerContext context, StateVertex currentState);

}
