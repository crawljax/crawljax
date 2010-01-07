package com.crawljax.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import com.crawljax.browser.BrowserFactory;
import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.browserwaiter.WaitConditionChecker;
import com.crawljax.condition.crawlcondition.CrawlConditionChecker;
import com.crawljax.condition.eventablecondition.EventableCondition;
import com.crawljax.condition.eventablecondition.EventableConditionChecker;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.condition.invariant.InvariantChecker;
import com.crawljax.core.configuration.CrawlSpecificationReader;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfigurationReader;
import com.crawljax.core.plugin.CrawljaxPluginsUtil;
import com.crawljax.core.state.Edge;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateMachine;
import com.crawljax.core.state.StateVertix;
import com.crawljax.oraclecomparator.OracleComparator;
import com.crawljax.oraclecomparator.StateComparator;
import com.crawljax.util.PropertyHelper;
import com.crawljax.util.database.HibernateUtil;

/**
 * The Crawljax Controller class is the core of Crawljax.
 * 
 * @author mesbah
 * @version $Id: CrawljaxController.java 6397 2009-12-29 14:31:57Z mesbah $
 */
public class CrawljaxController {
	private static final Logger LOGGER = Logger.getLogger(CrawljaxController.class.getName());
	private static int depth = 0;

	private int stateCounter = 1;
	private StateVertix indexState;
	private EmbeddedBrowser browser;
	private StateMachine stateMachine;
	private CrawlSession session;

	private long startCrawl;
	public static final ArrayList<Eventable> EXACTEVENTPATH = new ArrayList<Eventable>();
	private boolean fired = false;

	private List<Eventable> currentCrawlPath;

	private final String propertiesFile;

	private final StateComparator oracleComparator;
	private final InvariantChecker invariantChecker = new InvariantChecker();
	private final CrawlConditionChecker crawlConditionChecker = new CrawlConditionChecker();
	private final EventableConditionChecker eventableConditionChecker =
	        new EventableConditionChecker();

	private final WaitConditionChecker waitConditionChecker = new WaitConditionChecker();
	private Crawler crawler;
	private CandidateElementExtractor candidateElementExtractor;

	private final CrawljaxConfiguration crawljaxConfiguration;

	private final List<OracleComparator> comparatorsWithPreconditions;

	/**
	 * The constructor.
	 */
	public CrawljaxController() {
		this("crawljax.properties");
		LOGGER.warn("No custom setting is provided! Using the default settings.");
	}

	/**
	 * @param propertiesfile
	 *            the properties file.
	 */
	public CrawljaxController(final String propertiesfile) {
		this.propertiesFile = propertiesfile;
		this.crawljaxConfiguration = null;
		this.comparatorsWithPreconditions = new ArrayList<OracleComparator>();
		oracleComparator = new StateComparator(this.comparatorsWithPreconditions);
	}

	/**
	 * @param config
	 *            the crawljax configuration.
	 */
	public CrawljaxController(final CrawljaxConfiguration config) {
		this.propertiesFile = null;
		this.crawljaxConfiguration = config;
		CrawljaxConfigurationReader configReader = new CrawljaxConfigurationReader(config);
		CrawlSpecificationReader crawlerReader =
		        new CrawlSpecificationReader(configReader.getCrawlSpecification());
		this.comparatorsWithPreconditions = crawlerReader.getOracleComparators();
		oracleComparator = new StateComparator(crawlerReader.getOracleComparators());
		invariantChecker.setInvariants(crawlerReader.getInvariants());
		crawlConditionChecker.setCrawlConditions(crawlerReader.getCrawlConditions());
		waitConditionChecker.setWaitConditions(crawlerReader.getWaitConditions());
		eventableConditionChecker.setEventableConditions(configReader.getEventableConditions());
	}

	/**
	 * @throws ConfigurationException
	 *             if the configuration fails.
	 */
	private void init() throws ConfigurationException {
		LOGGER.info("Starting Crawljax...");
		LOGGER.info("Loading properties...");

		if (crawljaxConfiguration != null) {
			PropertyHelper.init(crawljaxConfiguration);
		} else {
			if (propertiesFile == null || propertiesFile.equals("")) {
				throw new ConfigurationException("No properties specified");
			}
			PropertyHelper.init(propertiesFile);
		}

		HibernateUtil.initialize();

		LOGGER.info("Used plugins:");

		CrawljaxPluginsUtil.loadPlugins();

		if (PropertyHelper.getCrawljaxConfiguration() != null) {
			CrawljaxPluginsUtil.runProxyServerPlugins(PropertyHelper.getCrawljaxConfiguration()
			        .getProxyConfiguration());
		}

		browser = BrowserFactory.getBrowser();
		LOGGER.info("Embedded browser implementation: " + browser.getClass().getName());
		crawler = new Crawler(browser, waitConditionChecker);
		candidateElementExtractor =
		        new CandidateElementExtractor(browser, eventableConditionChecker);

		LOGGER.info("Crawljax initialized!");
	}

	/**
	 * Run Crawljax.
	 * 
	 * @throws CrawljaxException
	 *             If the browser cannot be instantiated.
	 * @throws ConfigurationException
	 *             if crawljax configuration fails.
	 */
	public final void run() throws CrawljaxException, ConfigurationException {
		init();

		startCrawl = System.currentTimeMillis();

		CrawljaxPluginsUtil.runPreCrawlingPlugins(browser);

		try {
			crawler.goToInitialURL();
		} catch (CrawljaxException e) {
			LOGGER.fatal("Failed to load the site: " + e.getMessage(), e);
			throw e;
		}

		indexState =
		        new StateVertix(browser.getCurrentUrl(), "index", browser.getDom(),
		                oracleComparator.getStrippedDom(browser));

		stateMachine = new StateMachine(indexState);
		if (crawljaxConfiguration != null) {
			session = new CrawlSession(browser, stateMachine, indexState, crawljaxConfiguration);
		} else {
			session = new CrawlSession(browser, stateMachine, indexState);
		}

		CrawljaxPluginsUtil.runOnNewStatePlugins(session);

		LOGGER.info("Start crawling with " + PropertyHelper.getCrawlTagsValues().size()
		        + " tags and threshold-coefficient " + PropertyHelper.getCrawlThreholdValue());

		try {

			crawl();

		} catch (CrawljaxException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (OutOfMemoryError om) {
			LOGGER.error(om.getMessage(), om);
		}

		long timeCrawlCalc = System.currentTimeMillis() - startCrawl;

		browser.close();

		long timePageGenStart = System.currentTimeMillis();

		long timePageGenCalc = System.currentTimeMillis() - timePageGenStart;

		for (Eventable c : stateMachine.getStateFlowGraph().getAllEdges()) {
			LOGGER.info("Interaction Element= " + c.toString());
		}

		LOGGER.info("PERFORMANCE-> Crawl-time(ms): " + timeCrawlCalc);
		LOGGER.info("PERFORMANCE-> Genenration(ms): " + timePageGenCalc);
		LOGGER.info("EXAMINED ELEMENTS: "
		        + candidateElementExtractor.getNumberofExaminedElements());
		LOGGER.info("CLICKABLES: " + stateMachine.getStateFlowGraph().getAllEdges().size());
		LOGGER.info("STATES: " + stateMachine.getStateFlowGraph().getAllStates().size());
		LOGGER.info("Dom size (byte): "
		        + stateMachine.getStateFlowGraph().getMeanStateStringSize());

		LOGGER.info("Starting PostCrawlingPlugins...");

		CrawljaxPluginsUtil.runPostCrawlingPlugins(session);

		LOGGER.info("DONE!!!!");
	}

	/**
	 * Crawl through the clickables.
	 * 
	 * @throws CrawljaxException
	 *             if a failure occurs.
	 */
	private void crawl() throws CrawljaxException {
		checkConstraints();

		if (depth >= PropertyHelper.getCrawlDepthValue()
		        && PropertyHelper.getCrawlDepthValue() != 0) {
			LOGGER.info("DEPTH " + depth + " reached returning from rec call. Given depth: "
			        + PropertyHelper.getCrawlDepthValue());

			return;
		}

		extractCandidates();
	}

	/**
	 * Find candidate elements.
	 * 
	 * @throws CrawljaxException
	 *             if a failure occurs.
	 */
	private void extractCandidates() throws CrawljaxException {

		if (crawlConditionChecker.check(browser)) {
			LOGGER.info("Looking in state: " + stateMachine.getCurrentState().getName()
			        + " for candidate elements with ");
			clickTags(candidateElementExtractor.extract());

		} else {
			LOGGER.info("State " + stateMachine.getCurrentState().getName()
			        + " dit not satisfy the CrawlConditions.");
		}
	}

	/**
	 * Checks the state and time constraints.
	 * 
	 * @throws CrawljaxException
	 *             a crawljaxexception.
	 */
	private void checkConstraints() throws CrawljaxException {
		long timePassed = System.currentTimeMillis() - startCrawl;

		if ((PropertyHelper.getCrawlMaxTimeValue() != 0)
		        && (timePassed > PropertyHelper.getCrawlMaxTimeValue())) {
			throw new CrawljaxException("Max time " + PropertyHelper.getCrawlMaxTimeValue()
			        + " passed!");
		}

		if ((PropertyHelper.getCrawlMaxStatesValue() != 0)
		        && (stateMachine.getStateFlowGraph().getAllStates().size() >= PropertyHelper
		                .getCrawlMaxStatesValue())) {
			throw new CrawljaxException("Max number of states "
			        + PropertyHelper.getCrawlMaxStatesValue() + " reached!");
		}
	}

	/**
	 * @param elements
	 *            the list of candidate elements.
	 * @throws CrawljaxException
	 *             if an exception occurs.
	 */
	private void clickTags(final List<CandidateElement> elements) throws CrawljaxException {
		StateVertix currentHold = stateMachine.getCurrentState().clone();
		List<String> eventTypes = PropertyHelper.getRobotEventsValues();

		LOGGER.info("Starting preStateCrawlingPlugins...");
		CrawljaxPluginsUtil.runPreStateCrawlingPlugins(session, elements);

		boolean handleInputElements = true;
		for (CandidateElement candidateElement : elements) {
			EventableCondition eventableCondition = candidateElement.getEventableCondition();
			boolean conditionsSatisifed = true;
			if (eventableCondition != null) {
				conditionsSatisifed = eventableCondition.checkAllConditionsSatisfied(browser);
			}
			if (conditionsSatisifed) {
				for (String eventType : eventTypes) {
					Eventable eventable = new Eventable(candidateElement, eventType);
					eventable.setEdge(new Edge(currentHold, null));

					// load input element values
					if (handleInputElements) {
						crawler.handleInputElements(eventable);
						handleInputElements = false;
					}

					LOGGER.info("Firing " + eventable.getEventType() + " on element: "
					        + eventable + "; State: " + currentHold.getName());

					if (crawler.fireEvent(eventable)) {
						StateVertix newState =
						        new StateVertix(browser.getCurrentUrl(), getStateName(), browser
						                .getDom(), oracleComparator.getStrippedDom(browser));

						if (isDomChanged(currentHold, newState)) {
							fired = true;
							handleInputElements = true;
							boolean backTrack = true;

							LOGGER.info("Starting onNewStatePlugins...");

							// if eventable is last eventable in state, do not
							// backtrack
							if (candidateElement.equals(elements.get(elements.size() - 1))
							        && eventType.equals(eventTypes.get(eventTypes.size() - 1))) {
								backTrack = false;
							}
							updateStateMachine(currentHold, eventable, newState, backTrack);
						}
					}

				}
			} else {
				Eventable eventable = new Eventable(candidateElement, "");
				LOGGER.info("Conditions not satisfied for element: " + eventable + "; State: "
				        + currentHold.getName());

			}
		}
	}

	/**
	 * @param currentHold
	 *            the placeholder for the current stateVertix.
	 * @param event
	 *            the event edge.
	 * @param newState
	 *            the new state.
	 * @param backTrack
	 *            backtrack to a previous version.
	 * @throws CrawljaxException
	 *             an exception.
	 */
	private void updateStateMachine(final StateVertix currentHold, final Eventable event,
	        StateVertix newState, final boolean backTrack) throws CrawljaxException {

		EXACTEVENTPATH.add(event);

		StateVertix cloneState = stateMachine.addStateToCurrentState(newState, event);
		if (cloneState != null) {
			newState = cloneState.clone();
		}
		LOGGER.info("State " + newState.getName() + " added to the StateMachine.");
		stateMachine.changeState(newState);
		LOGGER.info("StateMachine's Pointer changed to: "
		        + stateMachine.getCurrentState().getName() + " FROM " + currentHold.getName());

		if (PropertyHelper.getTestInvariantsWhileCrawlingValue()) {
			checkInvariants(browser);
		}

		session.setCurrentState(newState);

		updateCrawlPath(currentHold, newState, event);

		if (cloneState == null) {
			CrawljaxPluginsUtil.runOnNewStatePlugins(session);
			depth++;

			LOGGER.info("RECURSIVE Call crawl; Current DEPTH= " + depth);
			crawl();
			depth--;
		}

		// go back to the previous state
		LOGGER.debug("AFTER: sm.current: " + stateMachine.getCurrentState().getName()
		        + " hold.current: " + currentHold.getName());

		stateMachine.changeState(currentHold);
		LOGGER.info("StateMachine's Pointer changed back to: "
		        + stateMachine.getCurrentState().getName());

		if (currentCrawlPath != null) {
			// only save crawlPath when an event is actually fired
			if (fired) {
				session.addCrawlPath(currentCrawlPath);
			}
			currentCrawlPath = null;
			fired = false;
		}

		if (backTrack) {
			// go back via previous clickables
			goBackExact(event);
		} else {
			// don't go back. only remove the currentEvent from the list
			EXACTEVENTPATH.remove(EXACTEVENTPATH.indexOf(event));
		}
	}

	/**
	 * @param stateBefore
	 *            the state before the event.
	 * @param stateAfter
	 *            the state after the event.
	 * @return true if the state is changed according to the compare method of the oracle.
	 */
	private boolean isDomChanged(final StateVertix stateBefore, final StateVertix stateAfter) {
		boolean isChanged = false;

		// do not need Oracle Comparators now, because hash of stripped domis
		// already calculated
		// isChanged = !oracleComparator.compare(stateBefore.getDom(),
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
	 * @return State name
	 */
	private String getStateName() {
		stateCounter++;
		String state = "state" + stateCounter;
		return state;
	}

	/**
	 * Adds current state and eventable to current crawlpath for exact test generation.
	 * 
	 * @param currentState
	 *            the current state.
	 * @param newState
	 *            the new state.
	 * @param eventable
	 *            the edge.
	 */
	private void updateCrawlPath(final StateVertix currentState, final StateVertix newState,
	        final Eventable eventable) {
		if (currentCrawlPath == null) {
			currentCrawlPath = new ArrayList<Eventable>();
		}
		currentCrawlPath.add(eventable);
	}

	/**
	 * @param currentEvent
	 *            the current clickable.
	 * @throws CrawljaxException
	 *             if a failure occurs.
	 */
	private void goBackExact(final Eventable currentEvent) throws CrawljaxException {
		LOGGER.info("Reloading Page for navigating back.");
		crawler.goToInitialURL();
		StateVertix curState = stateMachine.getInitialState();
		if (EXACTEVENTPATH.size() > 0) {
			// remove the currentEvent from the list
			EXACTEVENTPATH.remove(EXACTEVENTPATH.indexOf(currentEvent));
			if (EXACTEVENTPATH.size() > 0) {
				for (Eventable clickable : EXACTEVENTPATH) {
					// Thread.sleep(500);
					if (!crawlConditionChecker.check(browser)) {
						return;
					}
					LOGGER.info("Backtracking by firing " + clickable.getEventType()
					        + " on element: " + clickable);

					updateCrawlPath(curState, clickable.getTarget(), clickable);

					crawler.handleInputElements(clickable);
					crawler.fireEvent(clickable);
					if (!crawlConditionChecker.check(browser)) {
						return;
					}
					curState = clickable.getTarget();
					CrawljaxPluginsUtil.runOnRevisitStatePlugins(session, curState);
				}
			}
		}

	}

	/**
	 * Checks whether there are any invariants violated and runs the OnInvariantViolation plugins to
	 * process the failed invariants.
	 * 
	 * @param embeddedBrowser
	 *            the current browser instance
	 */
	private void checkInvariants(final EmbeddedBrowser embeddedBrowser) {
		if (!invariantChecker.check(embeddedBrowser)) {
			final List<Invariant> failedInvariants = invariantChecker.getFailedInvariants();
			for (Invariant failedInvariant : failedInvariants) {
				CrawljaxPluginsUtil.runOnInvriantViolationPlugins(failedInvariant, session);
			}
		}
	}
}
