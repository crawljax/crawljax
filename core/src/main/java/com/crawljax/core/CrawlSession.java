package com.crawljax.core;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.state.CrawlPath;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertex;
import com.crawljax.fragmentation.FragmentManager;

/**
 * Contains all data concerned with this crawl. There is one {@link CrawlSession} per crawl. Even if
 * there are multiple {@link EmbeddedBrowser}s configured.
 */
@Singleton
public class CrawlSession {
	private static final Logger LOGGER = LoggerFactory.getLogger(CrawlSession.class
	        .getName());

//	private Map<List<Eventable>, CrawlPathInfo> pathInfoMap = new HashMap<>();
//
//	public Map<List<Eventable>, CrawlPathInfo> getPathInfoMap() {
//		return pathInfoMap;
//	}
//
//	public void setPathInfoMap(Map<List<Eventable>, CrawlPathInfo> pathInfoMap) {
//		this.pathInfoMap = pathInfoMap;
//	}

	private final StateFlowGraph stateFlowGraph;

	/**
	 * This ConcurrentLinkedQueue holds all the Paths that are executed during the CrawlSession so
	 * far.
	 */
	private final Collection<List<Eventable>> crawlPaths =
			new ConcurrentLinkedQueue<>();

	private final StateVertex initialState;

	private final CrawljaxConfiguration config;

	/**
	 * Denote the time the CrawlSession Started in ms since 1970.
	 */
	private final long startTime;

	private final MetricRegistry registry;

	private FragmentManager fragmentManager;

	@Inject
	public CrawlSession(CrawljaxConfiguration config, FragmentManager fragmentManager, StateFlowGraph stateFlowGraph,
			StateVertex state, MetricRegistry registry) {
		this.fragmentManager = fragmentManager;
		this.stateFlowGraph = stateFlowGraph;
		this.initialState = state;
		this.config = config;
		this.registry = registry;
		this.startTime = new Date().getTime();
	}

	public FragmentManager getFragmentManager() {
		return fragmentManager;
	}

	public void setFragmentManager(FragmentManager fragmentManager) {
		this.fragmentManager = fragmentManager;
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
	 * @param crawlPath the eventable list
	 */
	public void addCrawlPath(CrawlPath crawlPath) {
		if(crawlPath.isEmpty()) {
			return;
		}
		
		LOGGER.info("Adding CrawlPath to session !!");
		String pathString = Crawler.printCrawlPath(crawlPath, true);

		this.crawlPaths.add(crawlPath);
//		int pathId = this.crawlPaths.size();
//		CrawlPathInfo pathInfo = new CrawlPathInfo(pathId, crawlPath.getBacktrackTarget(), crawlPath.isBacktrackSuccess(), crawlPath.isReachedNearDup());
//		pathInfo.setPathString(pathString);
//		pathInfoMap.put(crawlPath, pathInfo);
	}

	/**
	 * @return the initialState
	 */
	public StateVertex getInitialState() {
		return initialState;
	}

	/**
	 * @return the startTime
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * Remove the current path from the set of crawlPaths.
	 *
	 * @param path the path to be removed
	 */
	protected void removeCrawlPath(List<Eventable> path) {
		this.crawlPaths.remove(path);
	}

	public CrawljaxConfiguration getConfig() {
		return config;
	}

	public MetricRegistry getRegistry() {
		return registry;
	}
}
