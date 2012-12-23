package com.crawljax.plugins.savecrawlsession;

import java.util.List;
import java.util.Map;

import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateVertex;

/**
 * Version of CrawlSession that allows to be saved in an XML file.<br/>
 * IMPORTANT: This class should only be used by SaveCrawlSessionPlugin and SavedCrawlSessionReader.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $id$
 */
public class ExportableSession {

	private Map<String, StateVertex> mapStates;
	private Map<Long, Eventable> mapEventables;
	private List<Transition> transitions;
	private List<List<Transition>> crawlPaths;
	private String url;

	/**
	 * @param mapStates
	 *            a map with the states
	 * @param mapEventables
	 *            a map with the eventables (with Edge == null)
	 * @param transitions
	 *            another representation of the edges in the state-flow graph
	 * @param crawlPaths
	 *            the crawled paths expressed with transitions
	 * @param url
	 *            the crawled address
	 */
	public ExportableSession(Map<String, StateVertex> mapStates,
	        Map<Long, Eventable> mapEventables, List<Transition> transitions,
	        List<List<Transition>> crawlPaths, String url) {
		super();
		this.mapStates = mapStates;
		this.mapEventables = mapEventables;
		this.transitions = transitions;
		this.crawlPaths = crawlPaths;
		this.url = url;
	}

	/**
	 * Default constructor needed to save as xml.
	 */
	public ExportableSession() {

	}

	/**
	 * @return the map with states
	 */
	public Map<String, StateVertex> getMapStates() {
		return mapStates;
	}

	/**
	 * @param mapStates
	 *            a map with the states
	 */
	public void setMapStates(Map<String, StateVertex> mapStates) {
		this.mapStates = mapStates;
	}

	/**
	 * @return the map with Eventables (with edge == null)
	 */
	public Map<Long, Eventable> getMapEventables() {
		return mapEventables;
	}

	/**
	 * @param mapEventables
	 *            a map with the eventables (with Edge == null)
	 */
	public void setMapEventables(Map<Long, Eventable> mapEventables) {
		this.mapEventables = mapEventables;
	}

	/**
	 * @return the transitions (the edges) in the state-flow graph
	 */
	public List<Transition> getTransitions() {
		return transitions;
	}

	/**
	 * @param transitions
	 *            another representation of the edges in the state-flow graph
	 */
	public void setTransitions(List<Transition> transitions) {
		this.transitions = transitions;
	}

	/**
	 * @return the crawled paths
	 */
	public List<List<Transition>> getCrawlPaths() {
		return crawlPaths;
	}

	/**
	 * @param crawlPaths
	 *            the crawled paths expressed with transitions
	 */
	public void setCrawlPaths(List<List<Transition>> crawlPaths) {
		this.crawlPaths = crawlPaths;
	}

	/**
	 * @return the crawled url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            the crawled url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

}
