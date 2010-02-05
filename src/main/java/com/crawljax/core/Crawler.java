package com.crawljax.core;

import java.util.ArrayList;
import java.util.List;

import net.jcip.annotations.GuardedBy;

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
	private List<Eventable> exactEventPath = new ArrayList<Eventable>();

	/**
	 * TODO Stefan why is there two times the same variable? What is the difference and could it be
	 * merged? The path followed from the index to the current state.
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
	 * The sateMachine for this Crawler, keeping track of the path crawled by this Crawler. TODO
	 * Stefan its better to have this final...
	 */
	private StateMachine stateMachine;

	private final CrawljaxConfigurationReader configurationReader;

	private FormHandler formHandler;

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
	 * Crawler constructor for a new 'starting from scratch(index)' crawler.
	 * 
	 * @param mother
	 *            the main CrawljaxController
	 */
	public Crawler(CrawljaxController mother) {
		this(mother, new ArrayList<Eventable>());
		if (this.browser == null) {
			/**
			 * The Crawler is created with only a controller so probably its requested from the
			 * CrawljaxController Create a new Browser to prevent null pointers :). Creating a
			 * browser here would result in NOT loading the initial page in the run operation! This
			 * MUST be done by hand!
			 */
			try {
				browser = mother.getBrowserFactory().requestBrowser();
			} catch (InterruptedException e) {
				LOGGER.error("The request for a browser was interuped", e);
			}
		}
		/**
		 * Reset the state machine to null, dropping the existing state machine, as this call is
		 * from the CrawljaxController the initial State is not known yet and causes trouble. The
		 * CrawljaxController must create & set the first stateMachine using the setStateMachine
		 * which on his case checks is the stateMachine is not set for safety reasons.
		 */
		stateMachine = null;
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
	private Crawler(CrawljaxController mother, List<Eventable> returnPath) {
		this.exactEventPath = returnPath;
		this.controller = mother;
		stateMachine = controller.buildNewStateMachine();
		this.configurationReader = controller.getConfigurationReader();
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
		browser.goToUrl(configurationReader.getCrawlSpecificationReader().getSiteUrl());
		/**
		 * Thread safe
		 */
		controller.doBrowserWait(browser);
		CrawljaxPluginsUtil.runOnUrlLoadPlugins(browser);
	}

	/**
	 * Try to fire a given event on the Browser. TODO This method has been made public for the
	 * CrossBrowserTest only.
	 * 
	 * @param eventable
	 *            the eventable to fire
	 * @return true iff the event is fired
	 */
	private boolean fireEvent(Eventable eventable) {
		try {

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
				String newXPath = new ElementResolver(eventable, browser).resolve();
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

			if (browser.fireEvent(eventable)) {

				/**
				 * Let the controller execute its specified wait operation on the browser Thread
				 * safe
				 */
				controller.doBrowserWait(browser);

				/**
				 * Close opened windows
				 */
				browser.closeOtherWindows();

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

		// remove the currentEvent from the list
		if (exactEventPath.size() > 0) {
			for (Eventable clickable : exactEventPath) {

				if (!controller.getElementChecker().checkCrawlCondition(browser)) {
					return;
				}

				LOGGER.info("Backtracking by executing " + clickable.getEventType()
				        + " on element: " + clickable);

				stateMachine.changeState(clickable.getTargetStateVertix());

				curState = clickable.getTargetStateVertix();

				crawlPath.add(clickable);

				this.handleInputElements(clickable);

				if (this.fireEvent(clickable)) {

					// TODO ali, do not increase depth if eventable is from guidedcrawling
					depth++;

					/**
					 * Run the onRevisitStateValidator(s) TODO Stefan check for thread safety
					 */
					CrawljaxPluginsUtil.runOnRevisitStatePlugins(this.controller.getSession(),
					        curState);
				}

				if (!controller.getElementChecker().checkCrawlCondition(browser)) {
					return;
				}

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
	private ClickResult clickTag(final Eventable eventable, boolean handleInputElements)
	        throws CrawljaxException {

		// load input element values
		if (handleInputElements) {
			this.handleInputElements(eventable);
		}

		LOGGER.info("Executing " + eventable.getEventType() + " on element: " + eventable
		        + "; State: " + stateMachine.getCurrentState().getName());

		if (this.fireEvent(eventable)) {
			// String dom = new String(browser.getDom());
			StateVertix newState =
			        new StateVertix(browser.getCurrentUrl(), controller.getSession()
			                .getStateFlowGraph().getNewStateName(), browser.getDom(),
			                this.controller.getStripedDom(browser));

			if (isDomChanged(stateMachine.getCurrentState(), newState)) {
				crawlPath.add(eventable);
				if (stateMachine.update(eventable, newState, this.getBrowser(), this.controller
				        .getSession())) {
					// Dom changed
					// No Clone

					exactEventPath.add(eventable);

					CrawljaxPluginsUtil.runGuidedCrawlingPlugins(controller, controller
					        .getSession(), getExacteventpath(), this.stateMachine);

					return ClickResult.newState;
				} else {
					// Dom changed; Clone
					return ClickResult.cloneDetected;
				}
			}
		}
		// Event not fired or
		// Dom not changed
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
		StateVertix orrigionalState = stateMachine.getCurrentState();
		orrigionalState.searchForCandidateElements(candidateExtractor, configurationReader
		        .getTagElements(), configurationReader.getExcludeTagElements(),
		        configurationReader.getCrawlSpecificationReader().getClickOnce());

		LOGGER.info("Starting preStateCrawlingPlugins...");

		CrawljaxPluginsUtil.runPreStateCrawlingPlugins(controller.getSession(), orrigionalState
		        .getUnprocessedCandidateElements());

		boolean handleInputElements = true;

		for (CandidateCrawlAction action : orrigionalState) {
			CandidateElement candidateElement = action.getCandidateElement();
			EventType eventType = action.getEventType();

			if (candidateElement.allConditionsSatisfied(browser)) {
				ClickResult clickResult =
				        clickTag(new Eventable(candidateElement, eventType), handleInputElements);
				switch (clickResult) {
					case cloneDetected:
						fired = false;
						// TODO A optimisation could be to check the new state (== clone) to see
						// if there is unfinished work and continue with that so reload can be
						// Postponed and 1 reload can be saved.
						this.controller.getSession().addCrawlPath(crawlPath);
						if (orrigionalState.hasMoreToExplore()) {
							controller.addWorkToQueue(new Crawler(this.controller,
							        getCurrentExactPaths(false)));
						}
						return true;
					case newState:
						fired = true;
						// Recurse because new state found
						if (orrigionalState.hasMoreToExplore()) {
							controller.addWorkToQueue(new Crawler(this.controller,
							        getCurrentExactPaths(true)));
						}
						return newStateDetected(orrigionalState);
					case domUnChanged:
						// Dom not updated, continue with the next
						handleInputElements = false;
						break;
					default:
						break;
				}
			} else {

				LOGGER.info("Conditions not satisfied for element: " + candidateElement
				        + "; State: " + stateMachine.getCurrentState().getName());
			}
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
		stateMachine.changeState(orrigionalState);
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
		/**
		 * If the browser is null place a request for a browser from the BrowserFactory
		 */
		if (this.browser == null) {
			try {
				this.browser = controller.getBrowserFactory().requestBrowser();
			} catch (InterruptedException e1) {
				LOGGER.error("The request for a browser was interuped", e1);
			}
			LOGGER.info("Reloading page for navigating back since browser is not initialized.");
			try {
				this.goToInitialURL();
			} catch (Exception e) {
				LOGGER.error("Could not load the initialURL", e);
			}
		}

		// TODO Stefan ideally this should be placed in the constructor
		this.formHandler =
		        new FormHandler(browser, configurationReader.getInputSpecification(),
		                configurationReader.getCrawlSpecificationReader().getRandomInputInForms());

		this.candidateExtractor =
		        new CandidateElementExtractor(controller.getElementChecker(), this.getBrowser(),
		                formHandler);

		stateMachine.rewind();

		/**
		 * Do we need to go back into a previous state?
		 */
		if (exactEventPath.size() > 0) {
			try {
				this.goBackExact();
			} catch (Exception e) {
				LOGGER.error("Failed to backtrack", e);
			}
		}

	}

	/**
	 * Terminate and clean up this Crawler, release the acquired browser. Notice that other Crawlers
	 * might still be active. So this function does NOT shutdown all Crawlers active that should be
	 * done with {@link CrawlerExecutor#shutdown()}
	 */
	public void shutdown() {
		controller.getBrowserFactory().freeBrowser(this.browser);
	}

	/**
	 * The main function stated by the ExecutorService. Crawlers add themselves to the list by
	 * calling {@link CrawljaxController#addWorkToQueue(Crawler)}. When the ExecutorService finds a
	 * free thread this method is called and when this method ends the Thread is released again and
	 * a new Thread is started
	 * 
	 * @see java.util.concurrent.Executors#newFixedThreadPool(int)
	 * @see java.util.concurrent.ExecutorService {@inheritDoc}
	 */
	@Override
	public void run() {

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
			// TODO Stefan Delete the fired variable if possible? Or move this is not the correct
			// location.
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
	public final EmbeddedBrowser getBrowser() {
		return browser;
	}

	@Override
	public String toString() {
		return this.name;
	}

	/**
	 * Set the stateMachine that must be used, be careful! This must only be called during the init
	 * of the CrawljaxController.
	 * 
	 * @throws CrawljaxException
	 *             will be thrown when the stateMachine is already set!
	 * @param machine
	 *            the stateMachine to set.
	 */
	public void setStateMachine(final StateMachine machine) throws CrawljaxException {
		if (stateMachine != null) {
			throw new CrawljaxException(
			        "The stateMachine is allready specified can not be overwritten!");
		}
		this.stateMachine = machine;
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
	@GuardedBy("stateFlowGraph")
	private boolean checkConstraints() {
		long timePassed = System.currentTimeMillis() - controller.getSession().getStartTime();
		int maxCrawlTime = configurationReader.getCrawlSpecificationReader().getMaximumRunTime();
		if ((maxCrawlTime != 0) && (timePassed > maxCrawlTime * ONE_SECOND)) {

			/* remove all possible candidates left */
			// EXACTEVENTPATH.clear(); TODO Stefan: FIX this!
			LOGGER.info("Max time " + maxCrawlTime + " seconds passed!");
			/* stop crawling */
			return false;
		}
		StateFlowGraph graph = controller.getSession().getStateFlowGraph();
		// TODO Stefan is this needed?
		int maxNumberOfStates =
		        configurationReader.getCrawlSpecificationReader().getMaxNumberOfStates();
		synchronized (graph) {
			if ((maxNumberOfStates != 0) && (graph.getAllStates().size() >= maxNumberOfStates)) {
				/* remove all possible candidates left */
				// EXACTEVENTPATH.clear(); TODO Stefan: FIX this!

				LOGGER.info("Max number of states " + maxNumberOfStates + " reached!");

				/* stop crawling */
				return false;
			}
		}
		/* continue crawling */
		return true;
	}

}
