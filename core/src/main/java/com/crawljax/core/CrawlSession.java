/**
 * Created Jun 13, 2008
 */
package com.crawljax.core;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.crawljax.browser.BrowserPool;
import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.configuration.CrawljaxConfigurationReader;
import com.crawljax.core.state.CrawlPath;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertex;

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
	private final StateVertex initialState;

	/**
	 * Variable for reading the Configuration from.
	 */
	private final CrawljaxConfigurationReader crawljaxConfiguration;

	/**
	 * Denote the time the CrawlSession Started in ms since 1970.
	 */
	private final long startTime;

	private final ThreadLocal<CrawlPath> crawlPath = new ThreadLocal<CrawlPath>();

	/**
	 * ThreadLocal store the have a Thread<->Current State relation.
	 */
	private final ThreadLocal<StateVertex> tlState = new ThreadLocal<StateVertex>();

	/**
	 * The main BrowserPool where the current Browser is stored.
	 */
	private final BrowserPool browserPool;

	/**
	 * @param pool
	 *            the Embedded browser pool that is in use
	 */
	public CrawlSession(BrowserPool pool) {
		this(pool, null, null, 0);
	}

	/**
	 * @param pool
	 *            the embedded browser pool that is in use.
	 * @param stateFlowGraph
	 *            the state flow graph.
	 * @param state
	 *            the current state.
	 * @param startTime
	 *            the time this session started in milliseconds.
	 */
	public CrawlSession(BrowserPool pool, StateFlowGraph stateFlowGraph, StateVertex state,
	        long startTime) {
		this(pool, stateFlowGraph, state, startTime, null);
	}

	/**
	 * @param pool
	 *            the embedded browser instance pool that is in use.
	 * @param stateFlowGraph
	 *            the state flow graph
	 * @param state
	 *            the current state.
	 * @param startTime
	 *            the time this session started in milliseconds.
	 * @param crawljaxConfiguration
	 *            the configuration.
	 */
	public CrawlSession(BrowserPool pool, StateFlowGraph stateFlowGraph, StateVertex state,
	        long startTime, CrawljaxConfigurationReader crawljaxConfiguration) {
		this.crawljaxConfiguration = crawljaxConfiguration;
		this.browserPool = pool;
		this.stateFlowGraph = stateFlowGraph;
		this.initialState = state;
		this.startTime = startTime;
		tlState.set(state);
	}

	/**
	 * @return the browser or null if there is none
	 */
	public EmbeddedBrowser getBrowser() {
		return browserPool.getCurrentBrowser();
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
	public StateVertex getCurrentState() {
		StateVertex sv = tlState.get();
		if (sv == null) {
			tlState.set(getInitialState());
		} else {
			return sv;
		}
		return tlState.get();
	}

	/**
	 * @param currentState
	 *            the currentState to set
	 */
	public void setCurrentState(StateVertex currentState) {
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
	public final StateVertex getInitialState() {
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
	 * @deprecated use the {@link #getCurrentCrawlPath()}
	 */
	@Deprecated
	public List<Eventable> getExactEventPath() {
		return this.getCurrentCrawlPath();
	}

	/**
	 * @param exactEventPath
	 *            the exactEventPath to set
	 * @deprecated not used anymore...
	 */
	@Deprecated
	public void setExactEventPath(List<Eventable> exactEventPath) {
	}

	/**
	 * Remove the current path from the set of crawlPaths.
	 */
	protected final void removeCrawlPath() {
		List<Eventable> path = crawlPath.get();
		if (path == null) {
			return;
		}
		this.crawlPaths.remove(path);
	}

	/**
	 * branch the current crawl path, save the old-one and continue with the current.
	 */
	protected final void branchCrawlPath() {
		CrawlPath path = crawlPath.get();
		if (path == null) {
			return;
		}
		this.addCrawlPath(path.immutableCopy(false));
	}

	/**
	 * Add an eventable to the current crawl path.
	 * 
	 * @param clickable
	 *            the clickable to add to the current path.
	 */
	protected final void addEventableToCrawlPath(Eventable clickable) {
		CrawlPath path = crawlPath.get();
		if (path == null) {
			path = startNewPath();
		}
		path.add(clickable);
	}

	/**
	 * Get the current crawl path.
	 * 
	 * @return the current current crawl path.
	 */
	public CrawlPath getCurrentCrawlPath() {
		CrawlPath path = this.crawlPath.get();
		if (path == null) {
			return new CrawlPath();
		}
		return path;
	}

	/**
	 * start a new Path, because of the thread local every crawlPath is saved on the thread instead
	 * of on the Crawler, so without (re)starting a new Path the old path continues.
	 * 
	 * @return the new empty path.
	 */
	protected final CrawlPath startNewPath() {
		CrawlPath path = new CrawlPath();
		crawlPath.set(path);
		crawlPaths.add(path);
		return path;
	}

}
