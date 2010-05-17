package com.crawljax.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.configuration.CrawljaxConfigurationReader;
import com.crawljax.core.plugin.CrawljaxPluginsUtil;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateMachine;
import com.crawljax.core.state.StateVertix;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.forms.FormHandler;
import com.crawljax.forms.FormInput;
import com.crawljax.util.ElementResolver;

/**
 * Class that performs crawl actions. It is designed to be run inside a Thread
 * 
 * @see #run()
 * @author dannyroest@gmail.com (Danny Roest)
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id$
 */
public class Crawler implements Runnable {

	private static final Logger LOGGER = Logger.getLogger(Crawler.class.getName());

	private static final int ONE_SECOND = 1000;

	/**
	 * The main browser window 1 to 1 relation; Every Thread will get on browser assigned in the run
	 * function.
	 */
	private EmbeddedBrowser browser;

	/**
	 * The central DataController. This is a multiple to 1 relation Every Thread shares an instance
	 * of the same controller! All operations / fields used in the controller should be checked for
	 * Thread safety
	 */
	private final CrawljaxController controller;

	/**
	 * Depth register.
	 */
	private int depth = 0;

	/**
	 * The path followed from the index to the current state.
	 */
	private final List<Eventable> exactEventPath = new ArrayList<Eventable>();

	/**
	 * TODO Stefan why is there two times the same variable? What is the difference and could it be
	 * merged? The path followed from the index to the current state. Danny: From the state-flow
	 * graph one cannot derive which paths are crawled. This is for example required for regression
	 * testing.
	 */
	private final List<Eventable> crawlPath = new ArrayList<Eventable>();

	/**
	 * The utility which is used to extract the candidate clickables.
	 */
	private CandidateElementExtractor candidateExtractor;

	private boolean fired = false;

	/**
	 * The name of this Crawler when not default (automatic) this will be added to the Thread name
	 * in the {@link CrawlThreadFactory} as (name). In the
	 * {@link CrawlThreadFactory#newThread(Runnable)} the name is retrieved using the
	 * {@link #toString()} function.
	 * 
	 * @see Crawler#toString()
	 * @see CrawlThreadFactory#newThread(Runnable)
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
	 * Enum for describing what has happened after a {@link Crawler#clickTag(Eventable, boolean)}
	 * has been performed.
	 * 
	 * @see Crawler#clickTag(Eventable, boolean)
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
		this(mother, exactEventPath);
		this.name = name;
	}

	/**
	 * Private Crawler constructor for a 'reload' crawler. only used from internal
	 * 
	 * @param mother
	 *            the main CrawljaxController
	 * @param returnPath
	 *            the path used to return to the last state, this can be a empty list
	 */
	protected Crawler(CrawljaxController mother, List<Eventable> returnPath) {
		this.exactEventPath.addAll(returnPath);
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
	 * 
	 * @throws CrawljaxException
	 *             an exception when the index page can not be loaded
	 */
	public void goToInitialURL() throws CrawljaxException {
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
		try {

			// TODO Stefan; FindBugs found this bug, not yet solved
			// Should be changed with:
			// eventable.getIdentification().getHow().toString().equals("xpath")
			if (eventable.getIdentification().getHow().equals("xpath")
			        && eventable.getRelatedFrame().equals("")) {

				/**
				 * The path in the page to the 'clickable' (link, div, span, etc)
				 */
				String xpath = eventable.getIdentification().getValue();

				/**
				 * The type of event to execute on the 'clickable' like onClick, mouseOver, hover,
				 * etc
				 */
				EventType eventType = eventable.getEventType();

				/**
				 * Try to find a 'better' / 'quicker' xpath
				 */
				String newXPath = new ElementResolver(eventable, getBrowser()).resolve();
				if (newXPath != null) {
					if (!xpath.equals(newXPath)) {
						LOGGER.info("XPath changed from " + xpath + " to " + newXPath
						        + " relatedFrame:" + eventable.getRelatedFrame());
						eventable =
						        new Eventable(new Identification(Identification.How.xpath,
						                newXPath), eventType);
					}
				}
			}

			if (getBrowser().fireEvent(eventable)) {

				/**
				 * Let the controller execute its specified wait operation on the browser Thread
				 * safe
				 */
				controller.doBrowserWait(getBrowser());

				/**
				 * Close opened windows
				 */
				getBrowser().closeOtherWindows();

				return true; // A event fired
			} else {
				/**
				 * Execute the OnFireEventFailedPlugins with the current crawlPath with the
				 * crawlPath removed 1 state to represent the path TO here.
				 */
				int limit = crawlPath.size() - 1;
				if (limit < 0) {
					limit = 0;
				}
				CrawljaxPluginsUtil.runOnFireEventFailedPlugins(eventable, crawlPath.subList(0,
				        limit));
				return false; // no event fired
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		return false; // As we are here there was a error... so definitely there is no event fired.
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
	 * Reload the browser following the {@link #exactEventPath} to the given currentEvent.
	 * 
	 * @throws CrawljaxException
	 *             if the crawler encounters an error.
	 */
	private void goBackExact() throws CrawljaxException {
		/**
		 * Thread safe
		 */
		StateVertix curState = controller.getSession().getInitialState();

		for (Eventable clickable : exactEventPath) {

			if (!controller.getElementChecker().checkCrawlCondition(getBrowser())) {
				return;
			}

			LOGGER.info("Backtracking by executing " + clickable.getEventType() + " on element: "
			        + clickable);

			this.getStateMachine().changeState(clickable.getTargetStateVertix());

			curState = clickable.getTargetStateVertix();

			crawlPath.add(clickable);

			this.handleInputElements(clickable);

			if (this.fireEvent(clickable)) {

				// TODO ali, do not increase depth if eventable is from guidedcrawling
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
	 * @param handleInputElements
	 *            if inputs should be handled..
	 * @return the result of the click operation
	 * @throws CrawljaxException
	 *             an exception.
	 */
	private ClickResult clickTag(final Eventable eventable) throws CrawljaxException {
		// load input element values
		this.handleInputElements(eventable);

		LOGGER.info("Executing " + eventable.getEventType() + " on element: " + eventable
		        + "; State: " + this.getStateMachine().getCurrentState().getName());
		if (this.fireEvent(eventable)) {
			StateVertix newState =
			        new StateVertix(getBrowser().getCurrentUrl(), controller.getSession()
			                .getStateFlowGraph().getNewStateName(), getBrowser().getDom(),
			                this.controller.getStrippedDom(getBrowser()));

			if (isDomChanged(this.getStateMachine().getCurrentState(), newState)) {
				// Dom is changed, so data might need be filled in again
				crawlPath.add(eventable);
				// TODO Stefan; Fix this behaviour, this causes trouble + performance...
				this.controller.getSession().setExactEventPath(getExacteventpath());
				if (this.getStateMachine().update(eventable, newState, this.getBrowser(),
				        this.controller.getSession())) {
					// Dom changed
					// No Clone
					exactEventPath.add(eventable);

					CrawljaxPluginsUtil.runGuidedCrawlingPlugins(controller, controller
					        .getSession(), getExacteventpath(), this.getStateMachine());

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
	 */
	public final List<Eventable> getExacteventpath() {
		return exactEventPath;
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

	private void spawnThreads(StateVertix state, boolean removeLastStateFromEventPath) {
		Crawler c = null;
		do {
			if (c != null) {
				this.crawlQueueManager.addWorkToQueue(c);
			}
			c = new Crawler(this.controller, getCurrentExactPaths(removeLastStateFromEventPath));
		} while (state.registerCrawler(c));
	}

	private ClickResult crawlAction(CandidateCrawlAction action) throws CrawljaxException {
		CandidateElement candidateElement = action.getCandidateElement();
		EventType eventType = action.getEventType();

		StateVertix orrigionalState = this.getStateMachine().getCurrentState();

		if (candidateElement.allConditionsSatisfied(getBrowser())) {
			ClickResult clickResult = clickTag(new Eventable(candidateElement, eventType));
			switch (clickResult) {
				case cloneDetected:
					fired = false;
					// We are in the clone state so we continue with the cloned version to search
					// for work.
					this.controller.getSession().addCrawlPath(crawlPath);
					spawnThreads(orrigionalState, false);
					break;
				case newState:
					fired = true;
					// Recurse because new state found
					spawnThreads(orrigionalState, true);
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
		StateVertix orrigionalState = this.getStateMachine().getCurrentState();

		if (orrigionalState.searchForCandidateElements(candidateExtractor, configurationReader
		        .getTagElements(), configurationReader.getExcludeTagElements(),
		        configurationReader.getCrawlSpecificationReader().getClickOnce())) {
			// Only execute the preStateCrawlingPlugins when it's the first time
			LOGGER.info("Starting preStateCrawlingPlugins...");
			CrawljaxPluginsUtil.runPreStateCrawlingPlugins(controller.getSession(),
			        orrigionalState.getUnprocessedCandidateElements());
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
	private boolean newStateDetected(StateVertix orrigionalState) throws CrawljaxException {

		/**
		 * An event has been fired so we are one level deeper
		 */
		depth++;
		LOGGER.info("RECURSIVE Call crawl; Current DEPTH= " + depth);
		if (!this.crawl()) {
			// Crawling has stopped
			controller.terminate();
			return false;
		}
		this.getStateMachine().changeState(orrigionalState);
		return true;
	}

	/**
	 * Return the exactEventPath to be used in creating a new Crawler.
	 * 
	 * @param removeLastElement
	 *            if set to true the last element will not be in the crawlPath.
	 * @return the crawlPath leading to the current state.
	 */
	private ArrayList<Eventable> getCurrentExactPaths(boolean removeLastElement) {
		ArrayList<Eventable> path = new ArrayList<Eventable>();
		for (Eventable eventable : this.exactEventPath) {
			Eventable e = eventable.clone();
			path.add(e);
			// path.add(eventable);
		}
		// Remove the last entry because we want to be able to go back
		// into the original state where the last change (last in list)
		// was made

		if (removeLastElement && path.size() > 0) {
			path.remove(path.size() - 1);
		}

		return path;
	}

	/**
	 * Initialise the Crawler, retrieve a Browser and go to the initail url when no browser was
	 * present. rewind the state machine and goBack to the state if there is exactEventPath is
	 * specified.
	 */
	public void init() {
		this.browser = this.getBrowser();
		if (this.browser == null) {
			/**
			 * As the browser is null, request one and got to the initial url, if the browser is
			 * Already set the browser will be in the initial url.
			 */
			try {
				this.browser = controller.getBrowserFactory().requestBrowser();
			} catch (InterruptedException e1) {
				LOGGER.error("The request for a browser was interuped", e1);
			}
			LOGGER.info("Reloading page for navigating back");
			try {
				this.goToInitialURL();
			} catch (Exception e) {
				LOGGER.error("Could not load the initialURL", e);
			}
		}
		// TODO Stefan ideally this should be placed in the constructor
		this.formHandler =
		        new FormHandler(getBrowser(), configurationReader.getInputSpecification(),
		                configurationReader.getCrawlSpecificationReader().getRandomInputInForms());

		this.candidateExtractor =
		        new CandidateElementExtractor(controller.getElementChecker(), this.getBrowser(),
		                formHandler);
		/**
		 * go back into the previous state.
		 */
		try {
			this.goBackExact();
		} catch (Exception e) {
			LOGGER.error("Failed to backtrack", e);
		}
	}

	/**
	 * Terminate and clean up this Crawler, release the acquired browser. Notice that other Crawlers
	 * might still be active. So this function does NOT shutdown all Crawlers active that should be
	 * done with {@link CrawlerExecutor#shutdown()}
	 */
	public void shutdown() {
		controller.getBrowserFactory().freeBrowser(this.getBrowser());
	}

	/**
	 * The main function stated by the ExecutorService. Crawlers add themselves to the list by
	 * calling {@link CrawlQueueManager#addWorkToQueue(Crawler)}. When the ExecutorService finds a
	 * free thread this method is called and when this method ends the Thread is released again and
	 * a new Thread is started
	 * 
	 * @see java.util.concurrent.Executors#newFixedThreadPool(int)
	 * @see java.util.concurrent.ExecutorService {@inheritDoc}
	 */
	@Override
	public void run() {
		if (exactEventPath.size() > 0) {
			try {
				if (!exactEventPath.get(exactEventPath.size() - 1).getTargetStateVertix()
				        .startWorking(this)) {
					LOGGER.warn("BAH!");
					return;
				}
			} catch (CrawljaxException e) {
				LOGGER.error("Received Crawljax exception", e);
			}
		}

		/**
		 * Init the Crawler
		 */
		this.init();

		try {

			/**
			 * Hand over the main crawling
			 */
			this.crawl();

			/**
			 * Crawling is done; so the crawlPath of the current branch is known
			 */
			if (fired) {
				controller.getSession().addCrawlPath(crawlPath);
			}
		} catch (Exception e) {
			LOGGER.error("Crawl failed!", e);
		} finally {
			/**
			 * At last failure or non shutdown the Crawler.
			 */
			this.shutdown();
		}
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
	 * Test to see if the (new) dom is changed with regards to the old dom. This method is Thread
	 * safe.
	 * 
	 * @param stateBefore
	 *            the state before the event.
	 * @param stateAfter
	 *            the state after the event.
	 * @return true if the state is changed according to the compare method of the oracle.
	 */
	private boolean isDomChanged(final StateVertix stateBefore, final StateVertix stateAfter) {
		boolean isChanged = false;

		// do not need Oracle Comparators now, because hash of stripped dom is
		// already calculated
		// isChanged = !stateComparator.compare(stateBefore.getDom(),
		// stateAfter.getDom(), browser);
		isChanged = !stateAfter.equals(stateBefore);
		if (isChanged) {
			LOGGER.info("Dom is Changed!");
		} else {
			LOGGER.info("Dom Not Changed!");
		}

		return isChanged;
	}

	/**
	 * Checks the state and time constraints. This function is nearly Thread-safe
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
