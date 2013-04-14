package com.crawljax.core;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertex;

/**
 * The data about the crawlsession.
 */
@Singleton
public class CrawlSession {

	private static final Logger LOG = LoggerFactory.getLogger(CrawlSession.class);
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
	private final CrawljaxConfiguration crawljaxConfiguration;

	/**
	 * Denote the time the CrawlSession Started in ms since 1970.
	 */
	private final long startTime;

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
	@Inject
	public CrawlSession(StateFlowGraph stateFlowGraph, StateVertex state,
	        CrawljaxConfiguration crawljaxConfiguration) {
		this.crawljaxConfiguration = crawljaxConfiguration;
		this.stateFlowGraph = stateFlowGraph;
		this.initialState = state;
		this.startTime = new Date().getTime();
	}

	/**
	 * @return the stateFlowGraph
	 */
	public StateFlowGraph getStateFlowGraph() {
		return stateFlowGraph;
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
	public CrawljaxConfiguration getCrawljaxConfiguration() {
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
	 * Remove the current path from the set of crawlPaths.
	 */
	protected final void removeCrawlPath(List<Eventable> path) {
		this.crawlPaths.remove(path);
	}

	// /**
	// * branch the current crawl path, save the old-one and continue with the current.
	// */
	// protected final void branchCrawlPath() {
	// CrawlPath path = crawlPath.get();
	// if (path == null) {
	// return;
	// }
	// this.addCrawlPath(path.immutableCopy(false));
	// }

}
