package com.crawljax.plugins.savecrawlsession;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.plugin.GeneratesOutput;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateVertex;

/**
 * Plugin that saves the CrawlSession to an XML file which is used by RegressionTester.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $id$
 */
public class SaveCrawlSession implements PostCrawlingPlugin, GeneratesOutput {

	private String outputFolder = "";
	private final String sessionXml;
	private CrawlSession session;

	/**
	 * Creates a SaveCrawlsession plugin for RegressionTester which saves the CrawlSession to
	 * PropertyHelper.getOutputFolder() + "session.xml".
	 */
	public SaveCrawlSession() {
		this(Utils.SESSION_XML);
	}

	/**
	 * Creates a SaveCrawlsession plugin for RegressionTester which saves the CrawlSession to
	 * specified filename.
	 * 
	 * @param filenameSessionXML
	 *            the filename of the xml to write the crawlsession to
	 */
	public SaveCrawlSession(String filenameSessionXML) {
		this.sessionXml = filenameSessionXML;
	}

	@Override
	public void postCrawling(CrawlSession session) {
		this.session = session;
		try {
			ExportableSession savedSession =
			        new ExportableSession(getStates(), getEventables(), getTransitions(),
			                getCrawlPaths(), getUrl());
			XMLObject.objectToXML(savedSession, getOutputFolder() + sessionXml);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (CrawljaxException e) {
			e.printStackTrace();
		}
	}

	private void setEventableIds() {
		long id = 1;
		for (Eventable eventable : session.getStateFlowGraph().getAllEdges()) {
			eventable.setId(id);
			id++;
		}
	}

	private Map<String, StateVertex> getStates() {
		Map<String, StateVertex> mapStates = new HashMap<String, StateVertex>();
		for (StateVertex state : session.getStateFlowGraph().getAllStates()) {
			mapStates.put(state.getName(), state);
		}
		return mapStates;
	}

	private Map<Long, Eventable> getEventables() {
		setEventableIds();
		Map<Long, Eventable> mapEventables = new HashMap<Long, Eventable>();
		for (Eventable orgEventable : session.getStateFlowGraph().getAllEdges()) {
			Eventable eventable =
			        new Eventable(orgEventable.getElement().getNode(),
			                orgEventable.getEventType());
			eventable.setId(orgEventable.getId());
			eventable.setRelatedFormInputs(orgEventable.getRelatedFormInputs());
			mapEventables.put(eventable.getId(), eventable);
		}
		return mapEventables;
	}

	private List<List<Transition>> getCrawlPaths() throws CrawljaxException {
		List<List<Transition>> paths = new ArrayList<List<Transition>>();
		for (List<Eventable> eventablePath : session.getCrawlPaths()) {
			List<Transition> path = new ArrayList<Transition>();
			for (Eventable eventable : eventablePath) {
				path.add(new Transition(eventable.getSourceStateVertex().getName(), eventable
				        .getTargetStateVertex().getName(), eventable.getId()));
			}
			paths.add(path);
		}
		return paths;
	}

	private List<Transition> getTransitions() throws CrawljaxException {
		List<Transition> transitions = new ArrayList<Transition>();
		for (Eventable eventable : session.getStateFlowGraph().getAllEdges()) {
			transitions.add(new Transition(eventable.getSourceStateVertex().getName(), eventable
			        .getTargetStateVertex().getName(), eventable.getId()));
		}
		return transitions;
	}

	private String getUrl() throws CrawljaxException {
		return session.getCrawljaxConfiguration().getCrawlSpecificationReader().getSiteUrl();
	}

	@Override
	public String getOutputFolder() {
		return this.outputFolder;
	}

	@Override
	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;

	}

}
