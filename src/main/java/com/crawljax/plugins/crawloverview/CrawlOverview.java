package com.crawljax.plugins.crawloverview;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.plugin.PreStateCrawlingPlugin;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertex;
import com.crawljax.plugins.crawloverview.model.OutPutModel;
import com.crawljax.plugins.crawloverview.model.State;
import com.google.common.collect.Lists;
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

	private final CachedResources resources;
	private final OutputBuilder outputBuilder;
	private final Map<String, StateVertex> visitedStates;

	private CrawlSession session;

	private final OutPutModel outModel;

	public CrawlOverview(File outputFolder) {
		resources = new CachedResources();
		outputBuilder = new OutputBuilder(outputFolder, resources);
		outModel = new OutPutModel();
		visitedStates = Maps.newHashMap();
	}

	/**
	 * Logs all the canidate elements so that the plugin knows which elements were the candidate
	 * elements.
	 */
	@Override
	public void preStateCrawling(CrawlSession session, List<CandidateElement> candidateElements) {
		this.session = session;
		List<RenderedCandidateElement> newElements = Lists.newLinkedList();
		StateVertex state = session.getCurrentState();
		LOG.warn("Prestate found new state {}", state.getName());
		for (CandidateElement element : candidateElements) {
			WebElement webElement = getWebElement(session, element);
			if (webElement != null) {
				newElements.add(findElement(webElement, element));
			}
		}

		State stateOut = outModel.addStateIfAbsent(session.getCurrentState());
		stateOut.getCandidateElements().addAll(newElements);
	}

	private WebElement getWebElement(CrawlSession session, CandidateElement element) {
		WebElement webElement;
		try {
			// TODO Check if element.getIdentification().getValue() is correct replacement for
			// element.getXpath()
			webElement = session.getBrowser().getWebElement(element.getIdentification());
		} catch (Exception e) {
			LOG.info("Could not locate " + element.getElement().toString());
			return null;
		}
		return webElement;
	}

	private RenderedCandidateElement findElement(WebElement webElement, CandidateElement element) {
		Point location = webElement.getLocation();
		Dimension size = webElement.getSize();
		RenderedCandidateElement renderedCandidateElement =
		        // TODO Check if element.getIdentification().getValue() is correct replacement for
		        // element.getXpath()
		        new RenderedCandidateElement(element.getElement(), element.getIdentification()
		                .getValue(), location, size);
		return renderedCandidateElement;
	}

	/**
	 * Saves a screenshot of every new state.
	 */
	@Override
	public void onNewState(CrawlSession session) {
		this.session = session;
		StateVertex vertex = session.getCurrentState();
		State state = outModel.addStateIfAbsent(vertex);
		saveScreenshot(state.getName(), vertex);
	}

	private void saveScreenshot(String name, StateVertex vertex) {
		if (!visitedStates.containsKey(name)) {
			LOG.debug("Saving screenshot for state {}", name);
			File screenShot = outputBuilder.newScreenShotFile(name);
			try {
				session.getBrowser().saveScreenShot(screenShot);
			} catch (Exception e) {
				LOG.warn("Screenshots are not supported for {}", session.getBrowser());
			}
			visitedStates.put(name, vertex);
		}
	}

	/**
	 * Generated the report.
	 */
	@Override
	public void postCrawling(CrawlSession session) {
		StateFlowGraph sfg = session.getStateFlowGraph();
		outModel.setEdges(sfg.getAllEdges());
		outModel.checkForConsistency();
		try {
			writeIndexFile();
			StateWriter writer = new StateWriter(resources, outputBuilder, sfg, visitedStates);
			for (State state : outModel.getStates()) {
				writer.writeHtmlForState(state);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		LOG.info("Overview report generated: {}", outputBuilder.getIndexFile().getAbsolutePath());
	}

	private void writeIndexFile() throws Exception {
		LOG.debug("Writing index file");
		String template = resources.getIndexTemplate();
		VelocityContext context = new VelocityContext();
		String outModelJson = outModel.toJson();
		context.put("outputModel", outModelJson);

		// writing
		File fileHTML = outputBuilder.getIndexFile();
		outputBuilder.writeToFile(template, context, fileHTML, "index");
	}

}
