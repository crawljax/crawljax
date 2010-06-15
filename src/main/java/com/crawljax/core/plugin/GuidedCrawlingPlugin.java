package com.crawljax.core.plugin;

import java.util.List;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateMachine;
import com.crawljax.core.state.StateVertix;

/**
 * Plugin type that is called when the crawling control needs to be given to a plugin. After the
 * GuidedCrawling plugins are done, the control is returned back to the automatic pilot of Crawljax.
 * 
 * @author mesbah
 * @version $Id: GuidedCrawlingPlugin.java 6388 2009-12-29 13:36:00Z mesbah $
 */
public interface GuidedCrawlingPlugin extends Plugin {

	/**
	 * @param currentState
	 *            a copy of the currentState.
	 * @param controller
	 *            the crawljax controller instance.
	 * @param session
	 *            the crawl session.
	 * @param exactEventPaths
	 *            the exact crawled event paths. Used to bring the browser back to the state the
	 *            crawler was before guided crawling.
	 * @param stateMachine
	 *            the state machine.
	 */
	void guidedCrawling(StateVertix currentState, CrawljaxController controller,
	        CrawlSession session, List<Eventable> exactEventPaths, StateMachine stateMachine);

}
