package com.crawljax.core;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.configuration.CrawljaxConfigurationReader;
import com.crawljax.core.exception.BrowserConnectionException;
import com.crawljax.core.exception.CrawlPathToException;
import com.crawljax.core.plugin.CrawljaxPluginsUtil;
import com.crawljax.core.state.Attribute;
import com.crawljax.core.state.CrawlPath;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateMachine;
import com.crawljax.core.state.StateVertex;
import com.crawljax.forms.FormHandler;
import com.crawljax.forms.FormInput;
import com.crawljax.util.ElementResolver;

/**
 * Class that performs crawl actions. It is designed to run inside a Thread.
 * 
 * @see #run()
 */
public class Crawler implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(Crawler.class.getName());

	private static final int ONE_SECOND = 1000;

	/**
	 * The main browser window 1 to 1 relation; Every Thread will get on browser assigned in the run
	 * function.
	 */
	private EmbeddedBrowser browser;

	/**
	 * The central DataController. This is a multiple to 1 relation Every Thread shares an instance
	 * of the same controller! All operations / fields used in the controller should be checked for
	 * thread safety.
	 */
	private final CrawljaxController controller;

	/**
	 * Depth register.
	 */
	private int depth = 0;

	/**
	 * The path followed from the index to the current state.
	 */
	private final CrawlPath backTrackPath;

	/**
	 * The utility which is used to extract the candidate clickables.
	 */
	private CandidateElementExtractor candidateExtractor;

	private boolean fired = false;

	/**
	 * The name of this Crawler when not default (automatic) this will be added to the Thread name
	 * in the thread as (name). In the {@link CrawlerExecutor#beforeExecute(Thread, Runnable)} the
	 * name is retrieved using the {@link #toString()} function.
	 * 
	 * @see Crawler#toString()
	 * @see CrawlerExecutor#beforeExecute(Thread, Runnable)
	 */
	private String name = "";

	/**
	 * The sateMachine for this Crawler, keeping track of the path crawled by this Crawler.
	 */
	private final StateMachine stateMachine;

	private final CrawljaxConfigurationReader configurationReader;

	private FormHandler formHandler;

	/**
	 * The object to places calls to add new Crawlers or to remove one.
	 */
	private final CrawlQueueManager crawlQueueManager;

	/**
	 * Enum for describing what has happened after a {@link Crawler#clickTag(Eventable)} has been
	 * performed.
	 * 
	 * @see Crawler#clickTag(Eventable)
	 */
	private enum ClickResult {
		cloneDetected, newState, domUnChanged
	}

	/**
	 * @param mother
	 *            the main CrawljaxController
	 * @param exactEventPath
	 *            the event path up till this moment.
	 * @param name
	 *            a name for this crawler (default is empty).
	 */
	public Crawler(CrawljaxController mother, List<Eventable> exactEventPath, String name) {
		this(mother, new CrawlPath(exactEventPath));
		this.name = name;
	}

	/**
	 * Private Crawler constructor for a 'reload' crawler. Only used internally.
	 * 
	 * @param mother
	 *            the main CrawljaxController
	 * @param returnPath
	 *            the path used to return to the last state, this can be a empty list
	 * @deprecated better to use {@link #Crawler(CrawljaxController, CrawlPath)}
	 */
	@Deprecated
	protected Crawler(CrawljaxController mother, List<Eventable> returnPath) {
		this(mother, new CrawlPath(returnPath));
	}

	/**
	 * Private Crawler constructor for a 'reload' crawler. Only used internally.
	 * 
	 * @param mother
	 *            the main CrawljaxController
	 * @param returnPath
	 *            the path used to return to the last state, this can be a empty list
	 */
	protected Crawler(CrawljaxController mother, CrawlPath returnPath) {
		this.backTrackPath = returnPath;
		this.controller = mother;
		this.configurationReader = controller.getConfigurationReader();
		this.crawlQueueManager = mother.getCrawlQueueManager();
		if (controller.getSession() != null) {
			this.stateMachine =
			        new StateMachine(controller.getSession().getStateFlowGraph(), controller
			                .getSession().getInitialState(), controller.getInvariantList());
		} else {
			/**
			 * Reset the state machine to null, because there is no session where to load the
			 * stateFlowGraph from.
			 */
			this.stateMachine = null;
		}
	}

	/**
	 * Brings the browser to the initial state.
	 */
	public void goToInitialURL() {
		LOGGER.info("Loading Page "
		        + configurationReader.getCrawlSpecificationReader().getSiteUrl());
		getBrowser().goToUrl(configurationReader.getCrawlSpecificationReader().getSiteUrl());
		/**
		 * Thread safe
		 */
		controller.doBrowserWait(getBrowser());
		CrawljaxPluginsUtil.runOnUrlLoadPlugins(getBrowser());
	}

	/**
	 * Try to fire a given event on the Browser.
	 * 
	 * @param eventable
	 *            the eventable to fire
	 * @return true iff the event is fired
	 */
	private boolean fireEvent(Eventable eventable) {
		if (eventable.getIdentification().getHow().toString().equals("xpath")
		        && eventable.getRelatedFrame().equals("")) {

			/**
			 * The path in the page to the 'clickable' (link, div, span, etc)
			 */
			String xpath = eventable.getIdentification().getValue();

			/**
			 * The type of event to execute on the 'clickable' like onClick, mouseOver, hover, etc
			 */
			EventType eventType = eventable.getEventType();

			/**
			 * Try to find a 'better' / 'quicker' xpath
			 */
			String newXPath = new ElementResolver(eventable, getBrowser()).resolve();
			if (newXPath != null && !xpath.equals(newXPath)) {
				LOGGER.info("XPath changed from " + xpath + " to " + newXPath + " relatedFrame:"
				        + eventable.getRelatedFrame());
				eventable =
				        new Eventable(new Identification(Identification.How.xpath, newXPath),
				                eventType);
			}
		}

		if (getBrowser().fireEvent(eventable)) {

			/**
			 * Let the controller execute its specified wait operation on the browser thread safe.
			 */
			controller.doBrowserWait(getBrowser());

			/**
			 * Close opened windows
			 */
			getBrowser().closeOtherWindows();

			return true; // An event fired
		} else {
			/**
			 * Execute the OnFireEventFailedPlugins with the current crawlPath with the crawlPath
			 * removed 1 state to represent the path TO here.
			 */
			CrawljaxPluginsUtil.runOnFireEventFailedPlugins(eventable, controller.getSession()
			        .getCurrentCrawlPath().immutableCopy(true));
			return false; // no event fired
		}
	}

	/**
	 * Enters the form data. First, the related input elements (if any) to the eventable are filled
	 * in and then it tries to fill in the remaining input elements.
	 * 
	 * @param eventable
	 *            the eventable element.
	 */
	private void handleInputElements(Eventable eventable) {
		List<FormInput> formInputs = eventable.getRelatedFormInputs();

		for (FormInput formInput : formHandler.getFormInputs()) {
			if (!formInputs.contains(formInput)) {
				formInputs.add(formInput);
			}
		}
		eventable.setRelatedFormInputs(formInputs);
		formHandler.handleFormElements(formInputs);
	}

	/**
	 * Reload the browser following the {@link #backTrackPath} to the given currentEvent.
	 * 
	 * @throws CrawljaxException
	 *             if the {@link Eventable#getTargetStateVertex()} encounters an error.
	 */
	private void goBackExact() throws CrawljaxException {
		/**
		 * Thread safe
		 */
		StateVertex curState = controller.getSession().getInitialState();

		for (Eventable clickable : backTrackPath) {

			if (!controller.getElementChecker().checkCrawlCondition(getBrowser())) {
				return;
			}

			LOGGER.info("Backtracking by executing " + clickable.getEventType() + " on element: "
			        + clickable);

			this.getStateMachine().changeState(clickable.getTargetStateVertex());

			curState = clickable.getTargetStateVertex();

			controller.getSession().addEventableToCrawlPath(clickable);

			this.handleInputElements(clickable);

			if (this.fireEvent(clickable)) {

				depth++;

				/**
				 * Run the onRevisitStateValidator(s)
				 */
				CrawljaxPluginsUtil.runOnRevisitStatePlugins(this.controller.getSession(),
				        curState);
			}

			if (!controller.getElementChecker().checkCrawlCondition(getBrowser())) {
				return;
			}
		}
	}

	/**
	 * @param eventable
	 *            the element to execute an action on.
	 * @return the result of the click operation
	 * @throws CrawljaxException
	 *             an exception.
	 */
	private ClickResult clickTag(final Eventable eventable) throws CrawljaxException {
		// load input element values
		this.handleInputElements(eventable);

		// support for meta refresh tags
		if (eventable.getElement().getTag().toLowerCase().equals("meta")) {
			Pattern p = Pattern.compile("(\\d+);\\s+URL=(.*)");
			for (Attribute e : eventable.getElement().getAttributes()) {
				Matcher m = p.matcher(e.getValue());
				if (m.find()) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("URL:" + m.group(2));
					}
					try {
						// seconds*1000=ms
						Thread.sleep(Integer.parseInt(m.group(1)) * 1000);
					} catch (Exception ex) {
						LOGGER.error(ex.getLocalizedMessage(), ex);
					}
				}
			}
		}

		LOGGER.info("Executing " + eventable.getEventType() + " on element: " + eventable
		        + "; State: " + this.getStateMachine().getCurrentState().getName());
		if (this.fireEvent(eventable)) {
			StateVertex newState =
			        new StateVertex(getBrowser().getCurrentUrl(), controller.getSession()
			                .getStateFlowGraph().getNewStateName(), getBrowser().getDom(),
			                this.controller.getStrippedDom(getBrowser()));

			// checking if DOM is changed
			if (CrawljaxPluginsUtil.runDomChangeNotifierPlugins(this.getStateMachine()
			        .getCurrentState(), eventable, newState, getBrowser())) {

				// Dom changed
				controller.getSession().addEventableToCrawlPath(eventable);
				if (this.getStateMachine().update(eventable, newState, this.getBrowser(),
				        this.controller.getSession())) {

					// Change is no clone
					CrawljaxPluginsUtil.runGuidedCrawlingPlugins(controller, controller
					        .getSession(), controller.getSession().getCurrentCrawlPath(), this
					        .getStateMachine());

					return ClickResult.newState;
				} else {
					// Dom changed; Clone
					return ClickResult.cloneDetected;
				}
			}
		}
		// Event not fired or, Dom not changed
		return ClickResult.domUnChanged;
	}

	/**
	 * Return the Exacteventpath.
	 * 
	 * @return the exacteventpath
	 * @deprecated use {@link CrawlSession#getCurrentCrawlPath()}
	 */
	@Deprecated
	public final List<Eventable> getExacteventpath() {
		return controller.getSession().getCurrentCrawlPath();
	}

	/**
	 * Have we reached the depth limit?
	 * 
	 * @param depth
	 *            the current depth. Added as argument so this call can be moved out if desired.
	 * @return true if the limit has been reached
	 */
	private boolean depthLimitReached(int depth) {

		if (this.depth >= configurationReader.getCrawlSpecificationReader().getDepth()
		        && configurationReader.getCrawlSpecificationReader().getDepth() != 0) {
			LOGGER.info("DEPTH " + depth + " reached returning from rec call. Given depth: "
			        + configurationReader.getCrawlSpecificationReader().getDepth());
			return true;
		} else {
			return false;
		}
	}

	private void spawnThreads(StateVertex state) {
		Crawler c = null;
		do {
			if (c != null) {
				this.crawlQueueManager.addWorkToQueue(c);
			}
			c =
			        new Crawler(this.controller, controller.getSession().getCurrentCrawlPath()
			                .immutableCopy(true));
		} while (state.registerCrawler(c));
	}

	private ClickResult crawlAction(CandidateCrawlAction action) throws CrawljaxException {
		CandidateElement candidateElement = action.getCandidateElement();
		EventType eventType = action.getEventType();

		StateVertex orrigionalState = this.getStateMachine().getCurrentState();

		if (candidateElement.allConditionsSatisfied(getBrowser())) {
			ClickResult clickResult = clickTag(new Eventable(candidateElement, eventType));
			switch (clickResult) {
				case cloneDetected:
					fired = false;
					// We are in the clone state so we continue with the cloned version to search
					// for work.
					this.controller.getSession().branchCrawlPath();
					spawnThreads(orrigionalState);
					break;
				case newState:
					fired = true;
					// Recurse because new state found
					spawnThreads(orrigionalState);
					break;
				case domUnChanged:
					// Dom not updated, continue with the next
					break;
				default:
					break;
			}
			return clickResult;
		} else {

			LOGGER.info("Conditions not satisfied for element: " + candidateElement + "; State: "
			        + this.getStateMachine().getCurrentState().getName());
		}
		return ClickResult.domUnChanged;
	}

	/**
	 * Crawl through the clickables.
	 * 
	 * @throws CrawljaxException
	 *             if an exception is thrown.
	 */
	private boolean crawl() throws CrawljaxException {
		if (depthLimitReached(depth)) {
			return true;
		}

		if (!checkConstraints()) {
			return false;
		}

		// Store the currentState to be able to 'back-track' later.
		StateVertex orrigionalState = this.getStateMachine().getCurrentState();

		if (orrigionalState.searchForCandidateElements(candidateExtractor, configurationReader
		        .getTagElements(), configurationReader.getExcludeTagElements(),
		        configurationReader.getCrawlSpecificationReader().getClickOnce())) {
			// Only execute the preStateCrawlingPlugins when it's the first time
			LOGGER.info("Starting preStateCrawlingPlugins...");
			List<CandidateElement> candidateElements =
			        orrigionalState.getUnprocessedCandidateElements();
			CrawljaxPluginsUtil.runPreStateCrawlingPlugins(controller.getSession(),
			        candidateElements);
			// update crawlActions
			orrigionalState.filterCandidateActions(candidateElements);
		}

		CandidateCrawlAction action =
		        orrigionalState.pollCandidateCrawlAction(this, crawlQueueManager);
		while (action != null) {
			if (depthLimitReached(depth)) {
				return true;
			}

			if (!checkConstraints()) {
				return false;
			}
			ClickResult result = this.crawlAction(action);
			orrigionalState.finishedWorking(this, action);
			switch (result) {
				case newState:
					return newStateDetected(orrigionalState);
				case cloneDetected:
					return true;
				default:
					break;
			}
			action = orrigionalState.pollCandidateCrawlAction(this, crawlQueueManager);
		}
		return true;
	}

	/**
	 * A new state has been found!
	 * 
	 * @param orrigionalState
	 *            the current state
	 * @return true if crawling must continue false otherwise.
	 * @throws CrawljaxException
	 */
	private boolean newStateDetected(StateVertex orrigionalState) throws CrawljaxException {

		/**
		 * An event has been fired so we are one level deeper
		 */
		depth++;
		LOGGER.info("RECURSIVE Call crawl; Current DEPTH= " + depth);
		if (!this.crawl()) {
			// Crawling has stopped
			controller.terminate(false);
			return false;
		}
		this.getStateMachine().changeState(orrigionalState);
		return true;
	}

	/**
	 * Initialize the Crawler, retrieve a Browser and go to the initial URL when no browser was
	 * present. rewind the state machine and goBack to the state if there is exactEventPath is
	 * specified.
	 * 
	 * @throws InterruptedException
	 *             when the request for a browser is interrupted.
	 */
	public void init() throws InterruptedException {
		// Start a new CrawlPath for this Crawler
		controller.getSession().startNewPath();

		this.browser = this.getBrowser();
		if (this.browser == null) {
			/**
			 * As the browser is null, request one and got to the initial URL, if the browser is
			 * Already set the browser will be in the initial URL.
			 */
			this.browser = controller.getBrowserPool().requestBrowser();
			LOGGER.info("Reloading page for navigating back");
			this.goToInitialURL();
		}
		// TODO Stefan ideally this should be placed in the constructor
		this.formHandler =
		        new FormHandler(getBrowser(), configurationReader.getInputSpecification(),
		                configurationReader.getCrawlSpecificationReader().getRandomInputInForms());

		this.candidateExtractor =
		        new CandidateElementExtractor(controller.getElementChecker(), this.getBrowser(),
		                formHandler, configurationReader.getCrawlSpecificationReader());
		/**
		 * go back into the previous state.
		 */
		try {
			this.goBackExact();
		} catch (CrawljaxException e) {
			LOGGER.error("Failed to backtrack", e);
		}
	}

	/**
	 * Terminate and clean up this Crawler, release the acquired browser. Notice that other Crawlers
	 * might still be active. So this function does NOT shutdown all Crawlers active that should be
	 * done with {@link CrawlerExecutor#shutdown()}
	 */
	public void shutdown() {
		controller.getBrowserPool().freeBrowser(this.getBrowser());
	}

	/**
	 * The main function stated by the ExecutorService. Crawlers add themselves to the list by
	 * calling {@link CrawlQueueManager#addWorkToQueue(Crawler)}. When the ExecutorService finds a
	 * free thread this method is called and when this method ends the Thread is released again and
	 * a new Thread is started
	 * 
	 * @see java.util.concurrent.Executors#newFixedThreadPool(int)
	 * @see java.util.concurrent.ExecutorService
	 */
	@Override
	public void run() {
		if (!checkConstraints()) {
			// Constrains are not met at start of this Crawler, so stop immediately
			return;
		}
		if (backTrackPath.last() != null) {
			try {
				if (!backTrackPath.last().getTargetStateVertex().startWorking(this)) {
					return;
				}
			} catch (CrawljaxException e) {
				LOGGER.error("Received Crawljax exception", e);
			}
		}

		try {
			/**
			 * Init the Crawler
			 */
			try {
				this.init();
			} catch (InterruptedException e) {
				if (this.getBrowser() == null) {
					return;
				}
			}

			/**
			 * Hand over the main crawling
			 */
			if (!this.crawl()) {
				controller.terminate(false);
			}

			/**
			 * Crawling is done; so the crawlPath of the current branch is known
			 */
			if (!fired) {
				controller.getSession().removeCrawlPath();
			}
		} catch (BrowserConnectionException e) {
			// The connection of the browser has gone down, most of the times it means that the
			// browser process has crashed.
			LOGGER.error("Crawler failed because the used browser died during Crawling",
			        new CrawlPathToException("Crawler failed due to browser crash", controller
			                .getSession().getCurrentCrawlPath(), e));
			// removeBrowser will throw a RuntimeException if the current browser is the last
			// browser in the pool.
			this.controller.getBrowserPool().removeBrowser(this.getBrowser(),
			        this.controller.getCrawlQueueManager());
			return;
		} catch (CrawljaxException e) {
			LOGGER.error("Crawl failed!", e);
		}
		/**
		 * At last failure or non shutdown the Crawler.
		 */
		this.shutdown();
	}

	/**
	 * Return the browser used in this Crawler Thread.
	 * 
	 * @return the browser used in this Crawler Thread
	 */
	public EmbeddedBrowser getBrowser() {
		return browser;
	}

	@Override
	public String toString() {
		return this.name;
	}

	/**
	 * @return the state machine.
	 */
	public StateMachine getStateMachine() {
		return stateMachine;
	}

	/**
	 * Checks the state and time constraints. This function is nearly Thread-safe.
	 * 
	 * @return true if all conditions are met.
	 */
	private boolean checkConstraints() {
		long timePassed = System.currentTimeMillis() - controller.getSession().getStartTime();
		int maxCrawlTime = configurationReader.getCrawlSpecificationReader().getMaximumRunTime();
		if ((maxCrawlTime != 0) && (timePassed > maxCrawlTime * ONE_SECOND)) {

			LOGGER.info("Max time " + maxCrawlTime + " seconds passed!");
			/* stop crawling */
			return false;
		}
		StateFlowGraph graph = controller.getSession().getStateFlowGraph();
		int maxNumberOfStates =
		        configurationReader.getCrawlSpecificationReader().getMaxNumberOfStates();
		if ((maxNumberOfStates != 0) && (graph.getAllStates().size() >= maxNumberOfStates)) {
			LOGGER.info("Max number of states " + maxNumberOfStates + " reached!");
			/* stop crawling */
			return false;
		}
		/* continue crawling */
		return true;
	}

}
