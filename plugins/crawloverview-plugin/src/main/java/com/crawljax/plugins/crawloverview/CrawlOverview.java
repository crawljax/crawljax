package com.crawljax.plugins.crawloverview;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import com.crawljax.core.plugin.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.ExitNotifier.ExitStatus;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertex;
import com.crawljax.plugins.crawloverview.model.CandidateElementPosition;
import com.crawljax.plugins.crawloverview.model.OutPutModel;
import com.crawljax.plugins.crawloverview.model.State;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * The overview is a plug-in that generates a HTML report from the crawling session which can be
 * used to inspect what is crawled by Crawljax The report contains screenshots of the visited states
 * and the clicked elements are highlighted. The report also contains the state-flow graph in which
 * the visited states are linked together.
 **/
public class CrawlOverview implements OnNewStatePlugin, PreStateCrawlingPlugin,
        PostCrawlingPlugin, OnFireEventFailedPlugin, PreCrawlingPlugin {

	private static final Logger LOG = LoggerFactory.getLogger(CrawlOverview.class);

	private final ConcurrentMap<String, StateVertex> visitedStates;
	private final OutPutModelCache outModelCache;
	private OutputBuilder outputBuilder;
	private boolean warnedForElementsInIframe = false;

	private OutPutModel result;

	private HostInterface hostInterface;

	public CrawlOverview() {
		outModelCache = new OutPutModelCache();
		visitedStates = Maps.newConcurrentMap();
		LOG.info("Initialized the Crawl overview plugin");
		this.hostInterface = null;
	}

	public CrawlOverview(HostInterface hostInterface) {
		outModelCache = new OutPutModelCache();
		visitedStates = Maps.newConcurrentMap();
		LOG.info("Initialized the Crawl overview plugin");
		this.hostInterface = hostInterface;
	}

	@Override
	public void preCrawling(CrawljaxConfiguration config) throws RuntimeException {
		if(hostInterface == null) {
			hostInterface = new HostInterfaceImpl(config.getOutputDir(), null);
		}
		File outputFolder = hostInterface.getOutputDirectory();
		Preconditions.checkNotNull(outputFolder, "Output folder cannot be null");
		outputBuilder = new OutputBuilder(outputFolder);
	}

	/**
	 * Saves a screenshot of every new state.
	 */
	@Override
	public void onNewState(CrawlerContext context, StateVertex vertex) {
		LOG.debug("onNewState");
		StateBuilder state = outModelCache.addStateIfAbsent(vertex);
		visitedStates.putIfAbsent(state.getName(), vertex);
		saveScreenshot(context.getBrowser(), state.getName(), vertex);
		outputBuilder.persistDom(state.getName(), context.getBrowser().getUnStrippedDom());
	}

	private void saveScreenshot(EmbeddedBrowser browser, String name,
	        StateVertex vertex) {
		LOG.debug("Saving screenshot for state {}", name);
		File jpg = outputBuilder.newScreenShotFile(name);
		File thumb = outputBuilder.newThumbNail(name);
		try {
			byte[] screenshot = browser.getScreenShot();
			ImageWriter.writeScreenShotAndThumbnail(screenshot, jpg, thumb);
		} catch (CrawljaxException | WebDriverException e) {
			LOG.warn(
			        "Screenshots are not supported or not functioning for {}. Exception message: {}",
			        browser, e.getMessage());
			LOG.debug("Screenshot not made because {}", e.getMessage(), e);
		}
		LOG.trace("Screenshot saved");
	}

	/**
	 * Logs all the canidate elements so that the plugin knows which elements were the candidate
	 * elements.
	 */
	@Override
	public void preStateCrawling(CrawlerContext context,
	        ImmutableList<CandidateElement> candidateElements, StateVertex state) {
		LOG.debug("preStateCrawling");
		List<CandidateElementPosition> newElements = Lists.newLinkedList();
		LOG.info("Prestate found new state {} with {} candidates",
		        state.getName(), candidateElements.size());
		for (CandidateElement element : candidateElements) {
			try {
				WebElement webElement = getWebElement(context.getBrowser(), element);
				if (webElement != null) {
					newElements.add(findElement(webElement, element));
				}
			} catch (WebDriverException e) {
				LOG.info("Could not get position for {}", element, e);
			}
		}

		StateBuilder stateOut = outModelCache.addStateIfAbsent(state);
		stateOut.addCandidates(newElements);
		LOG.trace("preState finished, elements added to state");
	}

	private WebElement getWebElement(EmbeddedBrowser browser,
	        CandidateElement element) {
		try {
			if (!Strings.isNullOrEmpty(element.getRelatedFrame())) {
				warnUserForInvisibleElements();
				return null;
			} else {
				return browser.getWebElement(element.getIdentification());
			}
		} catch (WebDriverException e) {
			LOG.info("Could not locate element for positioning {}", element);
			return null;
		}
	}

	private void warnUserForInvisibleElements() {
		if (!warnedForElementsInIframe) {
			LOG.warn("Some elemnts are in an iFrame. We cannot display it in the Crawl overview");
			warnedForElementsInIframe = true;
		}
	}

	private CandidateElementPosition findElement(WebElement webElement,
	        CandidateElement element) {
		Point location = webElement.getLocation();
		Dimension size = webElement.getSize();
		CandidateElementPosition renderedCandidateElement =
		        new CandidateElementPosition(element.getIdentification().getValue(),
		                location, size);
		if (location.getY() < 0) {
			LOG.warn("Weird positioning {} for {}", webElement.getLocation(),
			        renderedCandidateElement.getXpath());
		}
		return renderedCandidateElement;
	}

	/**
	 * Generated the report.
	 */
	@Override
	public void postCrawling(CrawlSession session, ExitStatus exitStatus) {
		LOG.debug("postCrawling");
		StateFlowGraph sfg = session.getStateFlowGraph();
		result = outModelCache.close(session, exitStatus);
		outputBuilder.write(result, session.getConfig());
		StateWriter writer = new StateWriter(outputBuilder, sfg,
		        ImmutableMap.copyOf(visitedStates));
		for (State state : result.getStates().values()) {
			writer.writeHtmlForState(state);
		}
		LOG.info("Crawl overview plugin has finished");
	}

	/**
	 * @return the result of the Crawl or <code>null</code> if it hasn't finished yet.
	 */
	public OutPutModel getResult() {
		return result;
	}

	@Override
	public String toString() {
		return "Crawl overview plugin";
	}

	@Override
	public void onFireEventFailed(CrawlerContext context, Eventable eventable,
	        List<Eventable> pathToFailure) {
		outModelCache.registerFailEvent(context.getCurrentState(), eventable);
	}

}
