package com.crawljax.core.plugin;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.state.StateVertex;

/**
 * Plugin type that is called every time a state is revisited by Crawljax. Example: Benchmarking.
 * This plugin needs an explicit current state because the session.getCurrentState() does not
 * contain the correct current state since we are in back-tracking phase.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id$
 */
public interface OnRevisitStatePlugin extends Plugin {

	/**
	 * Method that is called every time a state is revisited by Crawljax. Warning: changing the
	 * state can influence crawljax, it is not a copy.
	 * 
	 * @param session
	 *            the crawlSession
	 * @param currentState
	 *            the state revisited
	 */
	void onRevisitState(CrawlSession session, StateVertex currentState);

}
