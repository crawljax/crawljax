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
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.plugin.GeneratesOutput;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.plugin.PreStateCrawlingPlugin;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertix;
import com.crawljax.util.Helper;

/**
 * Overviewplugin is a plugin that generates a HTML report from the crawling session which can be
 * used to inspect what is crawled by Crawljax The report contains screenshots of the visited states
 * and the clicked elements are highlighted. The report also contains the state-flow graph in which
 * the visited states are linked together. WARNING: This plugin is still in alpha development!
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id: CrawlOverview.java 17M 2011-10-03 21:22:37Z (local) $
 **/
public class CrawlOverview
        implements OnNewStatePlugin, PreStateCrawlingPlugin, PostCrawlingPlugin, GeneratesOutput {

	private static final Logger LOGGER = LoggerFactory.getLogger(CrawlOverview.class);

	private String outputFolder = "";

	private static final String MAIN_OUTPUTFOLDER = "crawloverview/";
	private static final String SCREENSHOTS_FOLDER = "screenshots/";
	private static final String STATES_FOLDER = "states/";

	private static final String RESOURCES_FOLDER = "plugins/crawloverview/";
	private static final String TEMPLATE_STATE = "state.vm";
	private static final String TEMPLATE_INDEX = "index.vm";
	private static final String JS_FOLDER = "js/";
	private static final String JS_GRAPH = "graph.js";
	private static final String JS_PROTOTYPE = "prototype-1.4.0.js";

	private static final int HEADER_SIZE = 20;
	private static final String COLOR_NEW_STATE = "green";
	private static final String COLOR_A_PREVIOUS_STATE = "#00FFFF";
	private static final String COLOR_NO_STATE_CHANGE = "orange";

	private static final Map<String, List<RenderedCandidateElement>> stateCandidatesMap =
	        new HashMap<String, List<RenderedCandidateElement>>();

	private CrawlSession session;

	private final List<String> visitedStates = new ArrayList<String>();

	/**
	 * Logs all the canidate elements so that the plugin knows which elements were the candidate
	 * elements.
	 */
	public void preStateCrawling(CrawlSession session, List<CandidateElement> candidateElements) {
		this.session = session;
		for (CandidateElement element : candidateElements) {
			findElementAndAddToMap(session.getCurrentState(), element);
		}
	}

	/**
	 * Saves a screenshot of every new state.
	 */
	public void onNewState(CrawlSession session) {
		this.session = session;
		saveScreenshot(session.getCurrentState());
	}

	/**
	 * Generated the report.
	 */
	public void postCrawling(CrawlSession session) {
		this.session = session;
		try {
			Helper.directoryCheck(getOutputFolder() + MAIN_OUTPUTFOLDER + STATES_FOLDER);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		generateOverviewReport();
	}

	private void saveScreenshot(StateVertix currentState) {
		if (!visitedStates.contains(currentState.getName())) {
			String fileName = getScreenShotFileName(currentState);
			try {
				Helper.directoryCheck(getOutputFolder() + MAIN_OUTPUTFOLDER + SCREENSHOTS_FOLDER);
				Helper.checkFolderForFile(fileName);
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}

			try {
				session.getBrowser().saveScreenShot(new File(fileName));
			} catch (Exception e) {
				LOGGER.warn("Screenshots are not supported for " + session.getBrowser());
			}

			visitedStates.add(currentState.getName());
		}
	}

	private void generateOverviewReport() {
		StateFlowGraph sfg = session.getStateFlowGraph();
		try {
			createNeededJavaScriptFiles();
			writeIndexFile();
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}

		for (StateVertix state : sfg.getAllStates()) {
			List<RenderedCandidateElement> rendered = stateCandidatesMap.get(state.getName());
			try {
				writeHtmlForState(state, rendered);
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		File indexFile = new File(getIndexFileName());
		LOGGER.info("Overview report generated: " + indexFile.getAbsolutePath());
	}

	private Eventable getEventableByCandidateElementInState(StateVertix state,
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

	private int getStateNumber(StateVertix state) {
		if (state.getName().equals("index")) {
			return 0;
		}
		return Integer.parseInt(state.getName().replace("state", ""));
	}

	private List<Map<String, String>> getElements(StateFlowGraph sfg, StateVertix state,
	        List<RenderedCandidateElement> rendered) {
		List<Map<String, String>> elements = new ArrayList<Map<String, String>>();

		if (rendered != null) {
			for (RenderedCandidateElement element : rendered) {
				Eventable eventable = getEventableByCandidateElementInState(state, element);
				StateVertix toState = null;
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
			eventableMap.put("from", eventable.getSourceStateVertix().getName());
			eventableMap.put("to", eventable.getTargetStateVertix().getName());
			eventables.add(eventableMap);
		}
		return eventables;
	}

	private List<Map<String, String>> getStates(StateFlowGraph sfg) {
		List<Map<String, String>> states = new ArrayList<Map<String, String>>();
		for (StateVertix stateVertix : sfg.getAllStates()) {
			Map<String, String> stateVertixMap = new HashMap<String, String>();
			stateVertixMap.put("name", stateVertix.getName());
			stateVertixMap.put("url", stateVertix.getUrl());
			stateVertixMap.put("id", stateVertix.getName().replace("state", "S"));
			stateVertixMap.put("screenshot", SCREENSHOTS_FOLDER + stateVertix.getName() + ".png");
			states.add(stateVertixMap);
		}
		return states;
	}

	private void createNeededJavaScriptFiles() throws IOException {
		Helper.directoryCheck(this.getOutputFolder() + MAIN_OUTPUTFOLDER + JS_FOLDER);
		File jsGraph =
		        new File(this.getOutputFolder() + MAIN_OUTPUTFOLDER + JS_FOLDER + JS_GRAPH);
		Helper.writeToFile(jsGraph.getAbsolutePath(),
		        Helper.getTemplateAsString(RESOURCES_FOLDER + JS_GRAPH), false);
		File jsPrototype =
		        new File(this.getOutputFolder() + MAIN_OUTPUTFOLDER + JS_FOLDER + JS_PROTOTYPE);
		Helper.writeToFile(jsPrototype.getAbsolutePath(),
		        Helper.getTemplateAsString(RESOURCES_FOLDER + JS_PROTOTYPE), false);
	}

	private void writeIndexFile() throws Exception {
		String fileName = getIndexFileName();
		Helper.checkFolderForFile(fileName);
		StateFlowGraph sfg = session.getStateFlowGraph();
		String template = Helper.getTemplateAsString(RESOURCES_FOLDER + TEMPLATE_INDEX);
		VelocityContext context = new VelocityContext();
		context.put("headerheight", HEADER_SIZE);

		context.put("eventables", getEventables(sfg));
		context.put("states", getStates(sfg));
		context.put("colorNewState", COLOR_NEW_STATE);
		context.put("colorPrevState", COLOR_A_PREVIOUS_STATE);
		context.put("colorNoStateChange", COLOR_NO_STATE_CHANGE);

		// writing
		File fileHTML = new File(fileName);
		FileWriter writer = new FileWriter(fileHTML);
		VelocityEngine ve = new VelocityEngine();
		/* disable logging */
		ve.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS,
		        "org.apache.velocity.runtime.log.NullLogChute");
		ve.evaluate(context, writer, "index", template);
		writer.flush();
		writer.close();
	}

	private void writeHtmlForState(StateVertix state, List<RenderedCandidateElement> rendered)
	        throws Exception {
		String fileName = getStateFileName(state);
		Helper.checkFolderForFile(fileName);
		StateFlowGraph sfg = session.getStateFlowGraph();

		String template = Helper.getTemplateAsString(RESOURCES_FOLDER + TEMPLATE_STATE);
		VelocityContext context = new VelocityContext();
		context.put("name", state.getName());
		context.put("screenshot", "../" + SCREENSHOTS_FOLDER + state.getName() + ".png");
		context.put("elements", getElements(sfg, state, rendered));

		// writing
		File fileHTML = new File(fileName);
		FileWriter writer = new FileWriter(fileHTML);
		VelocityEngine ve = new VelocityEngine();
		/* disable logging */
		ve.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS,
		        "org.apache.velocity.runtime.log.NullLogChute");
		ve.evaluate(context, writer, state.getName(), template);
		writer.flush();
		writer.close();
	}

	private void findElementAndAddToMap(StateVertix state, CandidateElement element) {
		// find element

		WebElement webElement;
		try {
			// TODO Check if element.getIdentification().getValue() is correct replacement for
			// element.getXpath()
			webElement = session.getBrowser().getWebElement(element.getIdentification());

		} catch (Exception e) {
			LOGGER.info("Could not locate " + element.getElement().toString());
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

	private String getScreenShotFileName(StateVertix state) {
		return getOutputFolder() + MAIN_OUTPUTFOLDER + SCREENSHOTS_FOLDER + state.getName()
		        + ".png";
	}

	private String getStateFileName(StateVertix state) {
		return getOutputFolder() + MAIN_OUTPUTFOLDER + STATES_FOLDER + state.getName() + ".html";
	}

	private String getIndexFileName() {
		return getOutputFolder() + MAIN_OUTPUTFOLDER + "index.html";
	}

	public String getOutputFolder() {
		return this.outputFolder;
	}

	public void setOutputFolder(String outputfolder) {
		this.outputFolder = outputfolder;
	}

}
