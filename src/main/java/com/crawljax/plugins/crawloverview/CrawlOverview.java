/*
 * CrawlOverview is a plugin for Crawljax that generates a nice HTML report to visually see the
 * inferred state graph. Copyright (C) 2010 crawljax.com This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.crawljax.plugins.crawloverview;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.plugin.PreStateCrawlingPlugin;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertex;
import com.google.common.collect.Maps;

/**
 * Overviewplugin is a plugin that generates a HTML report from the crawling session which can be
 * used to inspect what is crawled by Crawljax The report contains screenshots of the visited states
 * and the clicked elements are highlighted. The report also contains the state-flow graph in which
 * the visited states are linked together. WARNING: This plugin is still in alpha development!
 **/
public class CrawlOverview
        implements OnNewStatePlugin, PreStateCrawlingPlugin, PostCrawlingPlugin {

	private static final Logger LOG = LoggerFactory.getLogger(CrawlOverview.class);

	private static final int HEADER_SIZE = 20;
	private static final String COLOR_NEW_STATE = "green";
	private static final String COLOR_A_PREVIOUS_STATE = "#00FFFF";
	private static final String COLOR_NO_STATE_CHANGE = "orange";

	private final Map<String, List<RenderedCandidateElement>> stateCandidatesMap;
	private final CachedResources resources;
	private final OutputBuilder outputBuilder;
	private final Set<String> visitedStates = new HashSet<String>();

	private CrawlSession session;

	public CrawlOverview(File outputFolder) {
		stateCandidatesMap = Maps.newHashMap();
		resources = new CachedResources();
		outputBuilder = new OutputBuilder(outputFolder, resources);
	}

	/**
	 * Logs all the canidate elements so that the plugin knows which elements were the candidate
	 * elements.
	 */
	@Override
	public void preStateCrawling(CrawlSession session, List<CandidateElement> candidateElements) {
		this.session = session;
		for (CandidateElement element : candidateElements) {
			findElementAndAddToMap(session.getCurrentState(), element);
		}
	}

	/**
	 * Saves a screenshot of every new state.
	 */
	@Override
	public void onNewState(CrawlSession session) {
		this.session = session;
		saveScreenshot(session.getCurrentState());
	}

	/**
	 * Generated the report.
	 */
	@Override
	public void postCrawling(CrawlSession session) {
		StateFlowGraph sfg = session.getStateFlowGraph();
		try {
			writeIndexFile();
			for (StateVertex state : sfg.getAllStates()) {
				List<RenderedCandidateElement> rendered = stateCandidatesMap.get(state.getName());
				writeHtmlForState(state, rendered);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		LOG.info("Overview report generated: {}", outputBuilder.getIndexFile().getAbsolutePath());
	}

	private void saveScreenshot(StateVertex currentState) {
		if (!visitedStates.contains(currentState.getName())) {
			LOG.debug("Saving screenshot for state {}", currentState.getName());
			File screenShot = outputBuilder.newScreenShotFile(currentState.getName());
			try {
				session.getBrowser().saveScreenShot(screenShot);
			} catch (Exception e) {
				LOG.warn("Screenshots are not supported for {}", session.getBrowser());
			}
			visitedStates.add(currentState.getName());
		}
	}

	private Eventable getEventableByCandidateElementInState(StateVertex state,
	        RenderedCandidateElement element) {
		StateFlowGraph sfg = session.getStateFlowGraph();
		for (Eventable eventable : sfg.getOutgoingClickables(state)) {
			// TODO Check if element.getIdentification().getValue() is correct replacement for
			// element.getXpath()
			if (eventable.getIdentification().getValue()
			        .equals(element.getIdentification().getValue())) {
				return eventable;
			}
		}
		return null;
	}

	private int getStateNumber(StateVertex state) {
		if (state.getName().equals("index")) {
			return 0;
		}
		return Integer.parseInt(state.getName().replace("state", ""));
	}

	private List<Map<String, String>> getElements(StateFlowGraph sfg, StateVertex state,
	        List<RenderedCandidateElement> rendered) {
		List<Map<String, String>> elements = new ArrayList<Map<String, String>>();

		if (rendered != null) {
			for (RenderedCandidateElement element : rendered) {
				Eventable eventable = getEventableByCandidateElementInState(state, element);
				StateVertex toState = null;
				Map<String, String> elementMap = new HashMap<String, String>();
				if (eventable != null) {
					toState = sfg.getTargetState(eventable);
				}
				elementMap.put("left", "" + (element.getLocation().x - 1));
				elementMap.put("top", "" + (element.getLocation().y + 20 - 1));
				elementMap.put("width", "" + (element.getSize().width + 2));
				elementMap.put("height", "" + (element.getSize().height + 2));
				if (toState != null) {
					elementMap.put("targetname", toState.getName());
				} else {
					elementMap.put("targetname", "");
				}
				if (toState != null) {
					if (getStateNumber(toState) < getStateNumber(state)) {
						// state already found
						elementMap.put("color", COLOR_A_PREVIOUS_STATE);
					} else {
						// new state
						elementMap.put("color", COLOR_NEW_STATE);
					}
					elementMap.put("zindex", "20");
					elementMap.put("cursor", "pointer");
				} else {
					// no state change ater clicking or is not clicked
					elementMap.put("color", COLOR_NO_STATE_CHANGE);
					elementMap.put("cursor", "normal");
					elementMap.put("zindex", "10");
				}
				elements.add(elementMap);
			}
		}

		return elements;
	}

	private List<Map<String, String>> getEventables(StateFlowGraph sfg) throws CrawljaxException {
		List<Map<String, String>> eventables = new ArrayList<Map<String, String>>();
		for (Eventable eventable : sfg.getAllEdges()) {
			Map<String, String> eventableMap = new HashMap<String, String>();
			eventableMap.put("from", eventable.getSourceStateVertex().getName());
			eventableMap.put("to", eventable.getTargetStateVertex().getName());
			eventables.add(eventableMap);
		}
		return eventables;
	}

	private List<Map<String, String>> getStates(StateFlowGraph sfg) {
		List<Map<String, String>> states = new ArrayList<Map<String, String>>();
		for (StateVertex StateVertex : sfg.getAllStates()) {
			Map<String, String> StateVertexMap = new HashMap<String, String>();
			StateVertexMap.put("name", StateVertex.getName());
			StateVertexMap.put("url", StateVertex.getUrl());
			StateVertexMap.put("id", StateVertex.getName().replace("state", "S"));
			StateVertexMap.put("screenshot", StateVertex.getName() + ".png");
			states.add(StateVertexMap);
		}
		return states;
	}

	private void writeIndexFile() throws Exception {
		LOG.debug("Writing index file");
		StateFlowGraph sfg = session.getStateFlowGraph();
		String template = resources.getIndexTemplate();
		VelocityContext context = new VelocityContext();
		context.put("headerheight", HEADER_SIZE);

		context.put("eventables", getEventables(sfg));
		context.put("states", getStates(sfg));
		context.put("colorNewState", COLOR_NEW_STATE);
		context.put("colorPrevState", COLOR_A_PREVIOUS_STATE);
		context.put("colorNoStateChange", COLOR_NO_STATE_CHANGE);

		// writing
		File fileHTML = outputBuilder.getIndexFile();
		writeToFile(template, context, fileHTML, "index");
	}

	private void writeHtmlForState(StateVertex state, List<RenderedCandidateElement> rendered)
	        throws Exception {
		LOG.debug("Writing state file for state {}", state.getName());
		StateFlowGraph sfg = session.getStateFlowGraph();

		String template = resources.getStateTemplate();
		VelocityContext context = new VelocityContext();
		context.put("name", state.getName());
		context.put("screenshot", state.getName() + ".png");
		context.put("elements", getElements(sfg, state, rendered));

		// writing
		File fileHTML = outputBuilder.newStateFile(state.getName());
		String name = state.getName();
		writeToFile(template, context, fileHTML, name);
	}

	private void writeToFile(String template, VelocityContext context, File fileHTML, String name)
	        throws IOException {
		FileWriter writer = new FileWriter(fileHTML);
		VelocityEngine ve = new VelocityEngine();
		/* disable logging */
		ve.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
		        "org.apache.velocity.runtime.log.NullLogChute");
		ve.evaluate(context, writer, name, template);
		writer.flush();
		writer.close();
	}

	private void findElementAndAddToMap(StateVertex state, CandidateElement element) {
		// find element

		WebElement webElement;
		try {
			// TODO Check if element.getIdentification().getValue() is correct replacement for
			// element.getXpath()
			webElement = session.getBrowser().getWebElement(element.getIdentification());

		} catch (Exception e) {
			LOG.info("Could not locate " + element.getElement().toString());
			return;
		}
		// put in map
		if (!stateCandidatesMap.containsKey(state.getName())) {
			stateCandidatesMap.put(state.getName(), new ArrayList<RenderedCandidateElement>());
		}
		Point location = webElement.getLocation();
		Dimension size = webElement.getSize();
		RenderedCandidateElement renderedCandidateElement =
		// TODO Check if element.getIdentification().getValue() is correct replacement for
		// element.getXpath()
		        new RenderedCandidateElement(element.getElement(), element.getIdentification()
		                .getValue(), location, size);
		stateCandidatesMap.get(state.getName()).add(renderedCandidateElement);
	}

}
