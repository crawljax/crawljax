package com.crawljax.plugins.savecrawlsession;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertex;

/**
 * Reader class for saved crawl sessions.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $id$
 */
public class SessionReader {

	private ExportableSession savedCrawlSession;

	/**
	 * @param filename
	 *            the xml with the SavedCrawlSession
	 */
	public SessionReader(String filename) {
		try {
			this.savedCrawlSession = (ExportableSession) XMLObject.xmlToObject(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the state-flow graph from the saved CrawlSession
	 */
	public StateFlowGraph getStateFlowGraph() {
		StateFlowGraph sfg = null;
		for (StateVertex state : savedCrawlSession.getMapStates().values()) {
			if (sfg == null) {
				sfg = new StateFlowGraph(state);
			} else {
				sfg.addState(state, false);
			}
		}
		for (Transition transition : savedCrawlSession.getTransitions()) {
			sfg.addEdge(savedCrawlSession.getMapStates().get(transition.getFromState()),
			        savedCrawlSession.getMapStates().get(transition.getToState()),
			        savedCrawlSession.getMapEventables().get(transition.getEventableId()));
		}
		return sfg;
	}

	/**
	 * @return the crawled paths from the saved CrawlSession
	 */
	public List<List<Eventable>> getCrawlPaths() {
		List<List<Eventable>> paths = new ArrayList<List<Eventable>>();
		for (List<Transition> transitions : savedCrawlSession.getCrawlPaths()) {
			List<Eventable> path = new ArrayList<Eventable>();
			for (Transition transition : transitions) {
				Eventable e =
				        savedCrawlSession.getMapEventables().get(transition.getEventableId());
				// TODO Why is this null?
				if (e != null) {
					path.add(e);
				}
			}
			paths.add(path);
		}
		return paths;
	}

	/**
	 * @return the CrawlSpecification from the saved CrawlSession
	 */
	public CrawlSpecification getCrawlSpecification() {
		CrawlSpecification spec = new CrawlSpecification(savedCrawlSession.getUrl());
		spec.setMaximumStates(savedCrawlSession.getMapStates().size());
		spec.clickDefaultElements();
		return spec;
	}

	/**
	 * @return the crawled url from the saved CrawlSession
	 */
	public String getCrawlUrl() {
		return savedCrawlSession.getUrl();
	}
}
