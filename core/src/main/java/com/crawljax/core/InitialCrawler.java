package com.crawljax.core;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.plugin.Plugins;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateMachine;
import com.crawljax.core.state.StateVertex;

/**
 * This is the initial Crawler. An initial crawler crawls only the index page, creates the index
 * state and builds a session object and resumes the normal operations.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 */

public class InitialCrawler extends Crawler {

	private static final Logger LOGGER = LoggerFactory.getLogger(InitialCrawler.class);

	private final CrawljaxController controller;

	private EmbeddedBrowser browser; // should be final but try-catch prevents...

	private StateMachine stateMachine;

	private Plugins plugins;

	/**
	 * The default constructor.
	 * 
	 * @param mother
	 *            the controller to use.
	 */
	public InitialCrawler(CrawljaxController mother, Plugins plugins) {
		super(mother, new ArrayList<Eventable>(), "initial", plugins);
		controller = mother;
		this.plugins = plugins;
	}

	@Override
	public EmbeddedBrowser getBrowser() {
		return browser;
	}

	@Override
	public StateMachine getStateMachine() {
		return stateMachine;
	}

	@Override
	public void run() {

		try {
			browser = controller.getBrowserPool().requestBrowser();
		} catch (InterruptedException e) {
			LOGGER.error("The request for a browser was interuped.");
		}

		goToInitialURL();

		/**
		 * Build the index state
		 */
		StateVertex indexState =
		        new StateVertex(this.getBrowser().getCurrentUrl(), "index", this.getBrowser()
		                .getDom(), controller.getStrippedDom(this.getBrowser()));

		/**
		 * Build the StateFlowGraph
		 */
		StateFlowGraph stateFlowGraph = new StateFlowGraph(indexState);

		/**
		 * Build the StateMachine
		 */
		stateMachine =
		        new StateMachine(stateFlowGraph, indexState, controller.getInvariantList(),
		                plugins);

		/**
		 * Build the CrawlSession
		 */
		CrawlSession session =
		        new CrawlSession(controller.getBrowserPool(), stateFlowGraph, indexState,
		                controller.getStartCrawl(), controller.getConfiguration());
		controller.setSession(session);

		/**
		 * Run OnNewState Plugins for the index state.
		 */
		plugins.runOnNewStatePlugins(session);

		/**
		 * The initial work is done, continue with the normal procedure!
		 */
		super.run();

	}
}
