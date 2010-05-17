/**
 * Created Jun 13, 2008
 */
package com.crawljax.core;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.crawljax.browser.BrowserFactory;
import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.configuration.CrawljaxConfigurationReader;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertix;

/**
 * The data about the crawlsession.
 * 
 * @author mesbah
 * @version $Id$
 */
public class CrawlSession {

	/**
	 * This variable holds the current stateFlowGraph.
	 */
	private final StateFlowGraph stateFlowGraph;

	/**
	 * This ConcurrentLinkedQueue holds all the Paths that are executed during the CrawlSession so
	 * far.
	 */
	private final Collection<List<Eventable>> crawlPaths =
	        new ConcurrentLinkedQueue<List<Eventable>>();

	/**
	 * The initial State (indexState).
	 */
	private final StateVertix initialState;

	/**
	 * Variable for reading the Configuration from.
	 */
	private final CrawljaxConfigurationReader crawljaxConfiguration;

	/**
	 * Denote the time the CrawlSession Started in ms since 1970.
	 */
	private final long startTime;
	// TODO Stefan; optimise / change this behaviour this is not the most speedy solution
	private final ThreadLocal<List<Eventable>> exactEventPath =
	        new ThreadLocal<List<Eventable>>();

	/**
	 * ThreadLocal store the have a Thread<->Current State relation.
	 */
	private final ThreadLocal<StateVertix> tlState = new ThreadLocal<StateVertix>();

	/**
	 * The main BrowserFactory where the current Browser is stored.
	 */
	private final BrowserFactory browserFactory;

	/**
	 * @param factory
	 *            the Embedded browser factory that is in use
	 */
	public CrawlSession(BrowserFactory factory) {
		this(factory, null, null, 0);
	}

	/**
	 * @param factory
	 *            the embedded browser factory that is in use.
	 * @param stateFlowGraph
	 *            the state flow graph.
	 * @param state
	 *            the current state.
	 * @param startTime
	 *            the time this session started in milliseconds.
	 */
	public CrawlSession(BrowserFactory factory, StateFlowGraph stateFlowGraph, StateVertix state,
	        long startTime) {
		this(factory, stateFlowGraph, state, startTime, null);
	}

	/**
	 * @param factory
	 *            the embedded browser instance factory that is in use.
	 * @param stateFlowGraph
	 *            the state flow graph
	 * @param state
	 *            the current state.
	 * @param startTime
	 *            the time this session started in milliseconds.
	 * @param crawljaxConfiguration
	 *            the configuration.
	 */
	public CrawlSession(BrowserFactory factory, StateFlowGraph stateFlowGraph, StateVertix state,
	        long startTime, CrawljaxConfigurationReader crawljaxConfiguration) {
		this.crawljaxConfiguration = crawljaxConfiguration;
		this.browserFactory = factory;
		this.stateFlowGraph = stateFlowGraph;
		this.initialState = state;
		this.startTime = startTime;
		tlState.set(state);
	}

	/**
	 * @return the browser or null if there is none
	 */
	public EmbeddedBrowser getBrowser() {
		return browserFactory.getCurrentBrowser();
	}

	/**
	 * @return the stateFlowGraph
	 */
	public StateFlowGraph getStateFlowGraph() {
		return stateFlowGraph;
	}

	/**
	 * @return the currentState
	 */
	public StateVertix getCurrentState() {
		return tlState.get();
	}

	/**
	 * @param currentState
	 *            the currentState to set
	 */
	public void setCurrentState(StateVertix currentState) {
		this.tlState.set(currentState);
	}

	/**
	 * @return the crawlPaths
	 */
	public Collection<List<Eventable>> getCrawlPaths() {
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
	public CrawljaxConfigurationReader getCrawljaxConfiguration() {
		return crawljaxConfiguration;
	}

	/**
	 * @return the initialState
	 */
	public final StateVertix getInitialState() {
		return initialState;
	}

	/**
	 * @return the startTime
	 */
	public final long getStartTime() {
		return startTime;
	}

	/**
	 * @return the exactEventPath
	 */
	public List<Eventable> getExactEventPath() {
		return exactEventPath.get();
	}

	/**
	 * @param exactEventPath
	 *            the exactEventPath to set
	 */
	public void setExactEventPath(List<Eventable> exactEventPath) {
		this.exactEventPath.set(exactEventPath);
	}

}
