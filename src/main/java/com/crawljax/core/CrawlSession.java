/**
 * Created Jun 13, 2008
 */
package com.crawljax.core;

import java.util.ArrayList;
import java.util.List;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateMachine;
import com.crawljax.core.state.StateVertix;

/**
 * The data about the crawlsession.
 * 
 * @author mesbah
 * @version $Id$
 */
public class CrawlSession {

	private final EmbeddedBrowser browser;
	private final StateMachine stateMachine;
	private final List<List<Eventable>> crawlPaths = new ArrayList<List<Eventable>>();
	private StateVertix currentState;
	private final CrawljaxConfiguration crawljaxConfiguration;

	/**
	 * @param browser
	 *            the Embedded browser.
	 */
	public CrawlSession(EmbeddedBrowser browser) {
		this.browser = browser;
		this.stateMachine = null;
		this.currentState = null;
		this.crawljaxConfiguration = null;
	}

	/**
	 * @param browser
	 *            the embedded browser instance.
	 * @param stateMachine
	 *            the state machine.
	 * @param state
	 *            the current state.
	 */
	public CrawlSession(EmbeddedBrowser browser, StateMachine stateMachine, StateVertix state) {
		this.browser = browser;
		this.stateMachine = stateMachine;
		this.currentState = state;
		this.crawljaxConfiguration = null;
	}

	/**
	 * @param browser
	 *            the embedded browser instance.
	 * @param stateMachine
	 *            the state machine.
	 * @param state
	 *            the current state.
	 * @param crawljaxConfiguration
	 *            the configuration.
	 */
	public CrawlSession(EmbeddedBrowser browser, StateMachine stateMachine, StateVertix state,
	        CrawljaxConfiguration crawljaxConfiguration) {
		this.browser = browser;
		this.stateMachine = stateMachine;
		this.currentState = state;
		this.crawljaxConfiguration = crawljaxConfiguration;
	}

	/**
	 * @return the browser
	 */
	public EmbeddedBrowser getBrowser() {
		return browser;
	}

	/**
	 * @return the stateMachine
	 */
	public StateMachine getStateMachine() {
		return stateMachine;
	}

	/**
	 * @return the currentState
	 */
	public StateVertix getCurrentState() {
		return currentState;
	}

	/**
	 * @param currentState
	 *            the currentState to set
	 */
	public void setCurrentState(StateVertix currentState) {
		this.currentState = currentState;
	}

	/**
	 * @return the crawlPaths
	 */
	public List<List<Eventable>> getCrawlPaths() {
		return crawlPaths;
	}

	/**
	 * @param crawlPath
	 *            the eventable list
	 */
	public void addCrawlPath(List<Eventable> crawlPath) {
		this.crawlPaths.add(crawlPath);
	}

	/**
	 * @return the crawljaxConfiguration
	 */
	public CrawljaxConfiguration getCrawljaxConfiguration() {
		return crawljaxConfiguration;
	}

}
