package com.crawljax.plugins.crawloverview;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.plugin.PreStateCrawlingPlugin;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertex;
import com.crawljax.plugins.crawloverview.model.CandidateElementPosition;
import com.crawljax.plugins.crawloverview.model.OutPutModel;
import com.crawljax.plugins.crawloverview.model.State;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * The overview is a plug-in that generates a HTML report from the crawling session which can be
 * used to inspect what is crawled by Crawljax The report contains screenshots of the visited states
 * and the clicked elements are highlighted. The report also contains the state-flow graph in which
 * the visited states are linked together. WARNING: This plugin is still in alpha development!
 **/
public class CrawlOverview
        implements OnNewStatePlugin, PreStateCrawlingPlugin, PostCrawlingPlugin {

	private static final Logger LOG = LoggerFactory.getLogger(CrawlOverview.class);

	// private final CachedResources resources;
	private final OutputBuilder outputBuilder;
	private final Map<String, StateVertex> visitedStates;

	private OutPutModel result;

	private final OutPutModelCache outModelCache;

	public CrawlOverview(File outputFolder) {
		Preconditions.checkNotNull(outputFolder, "Output folder cannot be null");
		outputBuilder = new OutputBuilder(outputFolder);
		outModelCache = new OutPutModelCache();
		visitedStates = Maps.newHashMap();
		LOG.info("Initialized the Crawl overview plugin");
	}

	/**
	 * Saves a screenshot of every new state.
	 */
	@Override
	public void onNewState(CrawlSession session) {
		LOG.debug("onNewState");
		StateVertex vertex = session.getCurrentState();
		StateBuilder state = outModelCache.addStateIfAbsent(vertex);
		saveScreenshot(session, state.getName(), vertex);
		outputBuilder.persistDom(state.getName(), session.getBrowser().getDom());
		Point point = getOffSet(session.getBrowser());
		state.setScreenShotOffset(point);
		LOG.debug("{} has a body offset of {}", vertex.getName(), point);
	}

	private Point getOffSet(EmbeddedBrowser embeddedBrowser) {
		if (bodyHasOffset(embeddedBrowser)) {
			try {
				Number top =
				        (Number) embeddedBrowser
				                .executeJavaScript("return document.body.getBoundingClientRect().top;");
				Number left =
				        (Number) embeddedBrowser
				                .executeJavaScript("return document.body.getBoundingClientRect().left;");
				Point offset = new Point(left.intValue(), top.intValue());
				return offset;
			} catch (CrawljaxException e) {
				LOG.warn("Could not locate relative size of body, now using (0,0) instead", e);
			}
		}
		return new Point(0, 0);
	}

	private boolean bodyHasOffset(EmbeddedBrowser embeddedBrowser) {
		WebElement body = embeddedBrowser.getWebElement(new Identification(How.tag, "body"));
		String position = body.getCssValue("position");
		LOG.debug("Body has CSS position: {}", position);
		return "relative".equals(position);
	}

	private void saveScreenshot(CrawlSession session, String name, StateVertex vertex) {
		LOG.trace("Saving screenshot");
		synchronized (visitedStates) {
			if (!visitedStates.containsKey(name)) {
				visitedStates.put(name, vertex);
			}
		}
		LOG.debug("Saving screenshot for state {}", name);
		File jpg = outputBuilder.newScreenShotFile(name);
		File thumb = outputBuilder.newThumbNail(name);
		try {
			byte[] screenshot = session.getBrowser().getScreenShot();
			ImageWriter.writeScreenShotAndThumbnail(screenshot, jpg, thumb);
		} catch (CrawljaxException e) {
			LOG.warn("Screenshots are not supported for {}", session.getBrowser());
		}
		LOG.trace("Screenshot saved");
	}

	/**
	 * Logs all the canidate elements so that the plugin knows which elements were the candidate
	 * elements.
	 */
	@Override
	public void preStateCrawling(CrawlSession session, List<CandidateElement> candidateElements) {
		LOG.debug("preStateCrawling");
		List<CandidateElementPosition> newElements = Lists.newLinkedList();
		StateVertex state = session.getCurrentState();
		LOG.info("Prestate found new state {} with {} candidates", state.getName(),
		        candidateElements.size());
		for (CandidateElement element : candidateElements) {
			WebElement webElement = getWebElement(session, element);
			if (webElement != null) {
				newElements.add(findElement(webElement, element));
			}
		}

		StateBuilder stateOut = outModelCache.addStateIfAbsent(session.getCurrentState());
		stateOut.addCandidates(newElements);
		LOG.trace("preState finished, elements added to state");
	}

	private WebElement getWebElement(CrawlSession session, CandidateElement element) {
		try {
			// TODO Check if element.getIdentification().getValue() is correct replacement for
			// element.getXpath()
			return session.getBrowser().getWebElement(element.getIdentification());
		} catch (WebDriverException e) {
			LOG.info("Could not locate " + element.getElement().toString());
			return null;
		}
	}

	private CandidateElementPosition findElement(WebElement webElement, CandidateElement element) {
		Point location = webElement.getLocation();
		Dimension size = webElement.getSize();
		CandidateElementPosition renderedCandidateElement =
		        // TODO Check if element.getIdentification().getValue() is correct replacement for
		        // element.getXpath()
		        new CandidateElementPosition(element.getIdentification().getValue(), location,
		                size);
		return renderedCandidateElement;
	}

	/**
	 * Generated the report.
	 */
	@Override
	public void postCrawling(CrawlSession session) {
		LOG.debug("postCrawling");
		StateFlowGraph sfg = session.getStateFlowGraph();
		result = outModelCache.close(session);
		outputBuilder.write(result);
		synchronized (visitedStates) {
			StateWriter writer = new StateWriter(outputBuilder, sfg, visitedStates);
			for (State state : result.getStates().values()) {
				writer.writeHtmlForState(state);
			}
		}
		LOG.info("Crawl overview plugin has finished");
	}

	/**
	 * @return the result of the Crawl or <code>null</code> if it hasn't finished yet.
	 */
	public OutPutModel getResult() {
		return result;
	}

}
