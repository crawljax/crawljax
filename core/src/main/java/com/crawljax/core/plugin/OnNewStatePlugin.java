package com.crawljax.core.plugin;

import com.crawljax.core.CrawlSession;

/**
 * Plugin type that is called every time a new state is found by Crawljax. This also happens for the
 * Index State. Example: DOM validation.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
public interface OnNewStatePlugin extends Plugin {

	/**
	 * Method that is called when a new state is found. Warning: changing the session can change the
	 * behavior of Crawljax. It is not a copy!
	 * 
	 * @param session
	 *            the current session.
	 */
	void onNewState(CrawlSession session);

}
