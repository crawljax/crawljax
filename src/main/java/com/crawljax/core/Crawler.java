package com.crawljax.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.crawljax.browser.BrowserFactory;
import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.eventablecondition.EventableCondition;
import com.crawljax.condition.eventablecondition.EventableConditionChecker;
import com.crawljax.core.plugin.CrawljaxPluginsUtil;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.StateMachine;
import com.crawljax.core.state.StateVertix;
import com.crawljax.forms.FormHandler;
import com.crawljax.forms.FormInput;
import com.crawljax.util.ElementResolver;
import com.crawljax.util.PropertyHelper;

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

	private static final AtomicInteger TOTAL_THREAD_ID_COUNT = new AtomicInteger();

	private final int threadId;
	/**
	 * The main browser window 1 to 1 relation; Every Thread (instance of Crawljax) will get on
	 * browser assigned in the run function.
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
	private List<Eventable> crawlPath = new ArrayList<Eventable>();

	/**
	 * Restart from these candidates.
	 */
	private List<CandidateElement> candidates;

	/**
	 * Restart with this list of eventTypes.
	 */
	private List<String> eventTypes;

	/**
	 * The utility which is used to extract the candidate clickables.
	 */
	private final CandidateElementExtractor candidateExtractor;

	private boolean fired = false;

	private String name = "automatic";

	private boolean goBackExact = true;

	private StateMachine stateMachine;

	private String stateName;

	/**
	 * Crawler constructor for a new 'starting from scratch(index)' crawler.
	 * 
	 * @param mother
	 *            the main CrawljaxController
	 */
	public Crawler(CrawljaxController mother) {
		this(mother, false);
		if (this.browser == null) {
			/**
			 * The Crawler is created with only a controller so probably its requested from the
			 * CrawljaxController Create a new Browser to prevent null pointers :)
			 */
			browser = BrowserFactory.requestBrowser();
		}
		stateMachine = null;
	}

	/**
	 * @param mother
	 *            the main CrawljaxController
	 * @param exactEventPath
	 *            the event path up till this moment.
	 * @param reload
	 *            if true the browser first will be reloaded.
	 * @param goBackExact
	 *            true if the crawler should relaod to get to a previous state.
	 * @param name
	 *            a name for this crawler (default is automatic).
	 */
	public Crawler(CrawljaxController mother, List<Eventable> exactEventPath, boolean reload,
	        String name, boolean goBackExact) {
		this(mother, reload);
		this.goBackExact = goBackExact;
		this.exactEventPath = exactEventPath;
		this.name = name;
		LOGGER.info(getName() + " ExactPaths: " + exactEventPath.size());
		for (Eventable e : exactEventPath) {
			LOGGER.info("Eventable: " + e);
		}
	}

	private String getName() {

		return this.name + ": ";
	}

	/**
	 * Private crawler constructor for a new crawler. only used from internal
	 * 
	 * @param mother
	 *            the main CrawljaxController
	 * @param loadIndex
	 *            true if the index need to be loaded
	 */
	private Crawler(CrawljaxController mother, boolean loadIndex) {
		this.controller = mother;
		this.candidateExtractor = new CandidateElementExtractor(this);
		this.threadId = TOTAL_THREAD_ID_COUNT.incrementAndGet();
		stateMachine =
		        new StateMachine(controller.getStateFlowGraph(), controller.getIndexState());
		stateName = controller.getLastStateName();
		if (loadIndex) {
			/**
			 * The index page is requested to load so load a browser and reloads the initialURL
			 */
			browser = BrowserFactory.requestBrowser();
			try {
				goToInitialURL();
			} catch (Exception e) {
				LOGGER.fatal("Failed to load the site: " + e.getMessage(), e);
				System.exit(0);
			}
		}
	}

	/**
	 * Private Crawler constructor for a 'reload' crawler. only used from internal
	 * 
	 * @param mother
	 *            the main CrawljaxController
	 * @param currentState
	 *            the state the Crawler would go to to start
	 * @param returnPath
	 *            the path used to return to this eventable
	 * @param reThreadEventTypes
	 * @param reThreadElements
	 */
	private Crawler(CrawljaxController mother, ArrayList<Eventable> returnPath,
	        List<CandidateElement> reThreadElements, List<String> reThreadEventTypes) {
		this(mother, false);
		this.exactEventPath = returnPath;
		this.candidates = reThreadElements;
		this.eventTypes = reThreadEventTypes;
	}

	/**
	 * Brings the browser to the initial state.
	 * 
	 * @throws CrawljaxException
	 *             an exception when the index page can not be loaded
	 */
	public void goToInitialURL() throws CrawljaxException {
		LOGGER.info(getName() + "Loading Page " + PropertyHelper.getSiteUrlValue());
		browser.goToUrl(PropertyHelper.getSiteUrlValue());
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
	public boolean fireEvent(Eventable eventable) {
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
				String eventType = eventable.getEventType();

				/**
				 * Try to find a 'better' / 'quicker' xpath
				 */
				String newXPath = new ElementResolver(eventable, browser).resolve();
				if (newXPath != null) {
					if (!xpath.equals(newXPath)) {
						LOGGER.info(getName() + "XPath changed from " + xpath + " to " + newXPath
						        + " relatedFrame:" + eventable.getRelatedFrame());
						eventable =
						        new Eventable(new Identification("xpath", newXPath), eventType);
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

				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		return false;
	}

	/**
	 * Enters the form data. First, the related input elements (if any) to the eventable are filled
	 * in and then it tries to fill in the remaining input elements. TODO this function has been
	 * made public for CrossBrowserTester only
	 * 
	 * @param eventable
	 *            the eventable element.
	 */
	public void handleInputElements(Eventable eventable) {
		List<FormInput> formInputs = eventable.getRelatedFormInputs();

		FormHandler formHandler = new FormHandler(browser);
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
	 * @throws Exception
	 *             an exception when a Browser encounters an error
	 */
	private void goBackExact() {

		/**
		 * Thread safe
		 */
		StateVertix curState = controller.getIndexState();

		// remove the currentEvent from the list
		if (exactEventPath.size() > 0) {
			for (Eventable clickable : exactEventPath) {

				if (!controller.getCrawlConditionChecker().check(browser)) {
					return;
				}

				LOGGER.info(getName() + "Backtracking by executing " + clickable.getEventType()
				        + " on element: " + clickable);

				stateMachine.changeState(clickable.getTargetStateVertix());

				curState = clickable.getTargetStateVertix();

				crawlPath.add(clickable);

				this.handleInputElements(clickable);

				if (this.fireEvent(clickable)) {

					// TODO ali, do not increase depth if eventable is from guidedcrawling
					depth++;

					/**
					 * Run the onRevisitStateValidator(s) TODO Stefan check for thread safty
					 */
					CrawljaxPluginsUtil.runOnRevisitStatePlugins(this.controller.getSession(),
					        curState);
				}

				if (!controller.getCrawlConditionChecker().check(browser)) {
					return;
				}

			}
		}
	}

	/**
	 * @param eventable
	 *            the element to execute an action on.
	 * @param handleInputElements
	 *            if inputs should be handled.
	 * @param currentHold
	 *            the current state kept for comparison.
	 * @return 1 If Dom Changed and the new state is not found in the state machine. 0 If Dom
	 *         Changed and new state is a Clone. -1 If Dom is Not Changed at all.
	 * @throws CrawljaxException
	 *             an exception.
	 */
	public int clickTag(final Eventable eventable, boolean handleInputElements,
	        StateVertix currentHold) throws CrawljaxException {

		// load input element values
		if (handleInputElements) {
			this.handleInputElements(eventable);
		}

		LOGGER.info(getName() + "Executing " + eventable.getEventType() + " on element: "
		        + eventable + "; State: " + currentHold.getName());

		if (this.fireEvent(eventable)) {
			// String dom = new String(browser.getDom());
			StateVertix newState =
			        new StateVertix(browser.getCurrentUrl(), controller.getNewStateName(),
			                browser.getDom(), this.controller.getStripedDom(browser));

			if (controller.isDomChanged(currentHold, newState)) {
				crawlPath.add(eventable);
				if (stateMachine.update(currentHold, eventable, newState, this.getBrowser(),
				        this.controller)) {
					// Dom changed
					// No Clone

					// Fix the name of the new StateVertix
					// TODO Ali: why is this not done in the state machine itself?
					// this.stateName = controller.getNewStateName();
					// stateMachine.getCurrentState().setName(stateName);

					exactEventPath.add(eventable);

					CrawljaxPluginsUtil.runGuidedCrawlingPlugins(controller, controller
					        .getSession(), getExacteventpath());
					return 1;
				} else {
					// Dom changed
					// Clone
					return 0;
				}
			}
		}
		// Event not fired or
		// Dom not changed
		return -1;
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
	 * Crawl through the clickables.
	 * 
	 * @throws CrawljaxException
	 *             if an exception is thrown.
	 */
	private boolean crawl() throws CrawljaxException {
		if (!this.controller.checkConstraints()) {
			return false;
		}
		if (depth >= PropertyHelper.getCrawlDepthValue()
		        && PropertyHelper.getCrawlDepthValue() != 0) {
			LOGGER.info(getName() + "DEPTH " + depth
			        + " reached returning from rec call. Given depth: "
			        + PropertyHelper.getCrawlDepthValue());
			return true;
		}

		getCandidates();

		boolean resetTypes = true;
		if (this.eventTypes == null || this.eventTypes.size() == 0) {
			resetTypes = false;
			this.eventTypes = PropertyHelper.getRobotEventsValues();
		}

		StateVertix currentHold = stateMachine.getCurrentState().clone();

		LOGGER.info(getName() + "Starting preStateCrawlingPlugins...");
		CrawljaxPluginsUtil.runPreStateCrawlingPlugins(controller.getSession(), candidates);

		boolean handleInputElements = true;
		boolean reStoreCandidates = false;
		boolean reStoreEvents = false;
		boolean recursion = false;
		List<CandidateElement> reThreadElements = new ArrayList<CandidateElement>();
		List<String> reThreadEventTypes = new ArrayList<String>();
		for (CandidateElement candidateElement : candidates) {
			if (reStoreCandidates) {
				reThreadElements.add(candidateElement);
				continue;
			}
			EventableCondition eventableCondition = candidateElement.getEventableCondition();
			boolean conditionsSatisifed = true;
			if (eventableCondition != null) {
				conditionsSatisifed = eventableCondition.checkAllConditionsSatisfied(browser);
			}
			if (conditionsSatisifed) {
				for (String eventType : eventTypes) {
					if (reStoreEvents) {
						reThreadEventTypes.add(eventType);
						continue;
					}
					/**
					 * clickResult: 1 = Dom Changed & No Clone. 0 = Dom Changed & Clone. -1 = Dom
					 * Not Changed
					 */
					int clickResult =
					        clickTag(new Eventable(candidateElement, eventType),
					                handleInputElements, currentHold);

					if (clickResult >= 0) {

						if (clickResult == 0) {
							fired = false;
							// TODO Sometimes its possible to skip the reload....
							this.controller.getSession().addCrawlPath(crawlPath);

							// GO Back
							LOGGER.info(getName() + "Reloading Page for navigating back.");
							try {
								this.goToInitialURL();
							} catch (Exception e) {
								LOGGER.error("Could not reload the inital page after a CLONE", e);
							}

							/**
							 * Always do a state machine rewind because we are about to begin form
							 * scratch.
							 */
							depth = 0;
							stateMachine.rewind();
							this.crawlPath = new ArrayList<Eventable>();
							this.goBackExact();
							recursion = false;
						} else {
							fired = true;

							recursion = true;
							boolean lastCandidate =
							        candidateElement
							                .equals(candidates.get(candidates.size() - 1));
							boolean lastEventType =
							        eventType.equals(eventTypes.get(eventTypes.size() - 1));
							if (lastCandidate && lastEventType) {
								LOGGER.info(getName() + "No more items to process"
								        + " for this depth so not forking...");
							} else {
								if (!lastEventType) {
									// We were not at the last possible
									// eventType
									reStoreEvents = true;
									reThreadElements.add(candidateElement);
								}
								reStoreCandidates = true;
							}
						}
					} else {
						// Dom not yet updated
						// continue with the next
						handleInputElements = false;
					}
				}
				reStoreEvents = false;
			} else {
				Eventable eventable = new Eventable(candidateElement, "");
				LOGGER.info(getName() + "Conditions not satisfied for element: " + eventable
				        + "; State: " + stateMachine.getCurrentState().getName());
			}

			if (resetTypes) {
				resetTypes = false;
				this.eventTypes = PropertyHelper.getRobotEventsValues();
			}
		}
		if (reStoreCandidates) {
			/**
			 * Make clone of everything that might be reused
			 */

			controller.addWorkToQueue(new Crawler(this.controller, getCurrentExactPaths(true),
			        reThreadElements, reThreadEventTypes));
			// this.currentState = currentHold;
		}
		if (recursion) {
			/**
			 * Reset the 'restart' data
			 */
			this.candidates = null;
			this.eventTypes = null;

			/**
			 * An event has been fired so we are one level deeper
			 */
			depth++;
			LOGGER.info(getName() + "RECURSIVE Call crawl; Current DEPTH= " + depth);
			if (!this.crawl()) {
				// Crawling has stopped
				controller.terminate();
				return false;
			}
			stateMachine.changeState(currentHold);
		}

		return true;
	}

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

	private void getCandidates() throws CrawljaxException {
		if (this.candidates == null) {
			if (controller.getCrawlConditionChecker().check(browser)) {
				LOGGER.info(getName() + "Looking in state: "
				        + stateMachine.getCurrentState().getName()
				        + " for candidate elements with ");
				this.candidates =
				        this.candidateExtractor.extract(PropertyHelper.getCrawlTagElements(),
				                PropertyHelper.getCrawlExcludeTagElements(), PropertyHelper
				                        .getClickOnceValue());
			} else {
				LOGGER.info(getName() + "State " + stateMachine.getCurrentState().getName()
				        + " dit not satisfy the CrawlConditions.");
				this.candidates = new ArrayList<CandidateElement>();
			}
		}
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
		 * If the browser is null place a request for a browser from the BrowserFactory
		 */
		if (this.browser == null) {
			this.browser = BrowserFactory.requestBrowser();
			LOGGER.info(getName()
			        + "Reloading Page for navigating back since browser is not initialized.");
			try {
				this.goToInitialURL();
			} catch (Exception e) {
				LOGGER.error("Could not load the initialURL", e);
				e.printStackTrace();
			}
		}

		stateMachine.rewind();

		/**
		 * Do we need to go back into a previous state?
		 */
		if (exactEventPath.size() > 0 && this.goBackExact) {
			try {
				this.goBackExact();
			} catch (Exception e) {
				LOGGER.error(getName() + "Failed to backtrack", e);
			}
		}

		try {

			/**
			 * Hand over the main crawling
			 */

			this.crawl();

			/**
			 * Crawling is done; so the crawlPath of the current branch is known
			 */
			// TODO
			if (fired) {
				controller.getSession().addCrawlPath(crawlPath);
			}
		} catch (Exception e) {
			LOGGER.error(getName() + "Crawl failed!", e);
		} finally {
			/**
			 * At last failure or non release the browser
			 */
			BrowserFactory.freeBrowser(this.browser);
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

	/**
	 * @return the eventable condition checker.
	 */
	public EventableConditionChecker getEventableConditionChecker() {
		return this.controller.getEventableConditionChecker();
	}

	/**
	 * increase the number of checked elements, as a statistics measure to know how many elements
	 * were actually examined.
	 */
	public void increaseNumberExaminedElements() {
		this.controller.increaseNumberExaminedElements();
	}

	/**
	 * Check if a given element is already checked, preventing duplicate work. Passing the request
	 * to the CrawljaxController.
	 * 
	 * @see CrawljaxController#elementIsAlreadyChecked(String)
	 * @param element
	 *            the to search for if its already checked
	 * @return true if the element is already checked
	 */
	public boolean elementIsAlreadyChecked(String element) {
		return this.controller.elementIsAlreadyChecked(element);
	}

	/**
	 * Mark a given element as checked to prevent duplicate work. Passing the operation to
	 * Controller.
	 * 
	 * @see CrawljaxController#markElementAsChecked(String)
	 * @param element
	 *            the elements that is checked
	 */
	public void markElementAsChecked(String element) {
		controller.markElementAsChecked(element);
	}

	@Override
	public String toString() {
		return "Crawler Thread " + this.threadId + ": " + this.getName();
	}

	/**
	 * Set the stateMachine that must be used, becarefull! This must only be called during the init
	 * of the CrawljaxController.
	 * 
	 * @throws CrawljaxException
	 *             will be thrown when the stateMachine is allready set!
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
	 * Request a lock on the examinedElements datastructure. Becarefull a requested lock MUST be
	 * returned by hand! using the {@link #releaseExaminedElementsMutex()}
	 * 
	 * @see Crawler#releaseExaminedElementsMutex()
	 */
	public void requestExaminedElementsMutex() {
		controller.requestExaminedElementsMutex();
	}

	/**
	 * Release the lock for the examinedElements datastructure.
	 */
	public void releaseExaminedElementsMutex() {
		controller.releaseExaminedElementsMutex();
	}

	/**
	 * @return the state machine.
	 */
	public StateMachine getStateMachine() {
		return stateMachine;
	}

}
