package com.crawljax.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import net.jcip.annotations.GuardedBy;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import com.crawljax.browser.BrowserFactory;
import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.browserwaiter.WaitConditionChecker;
import com.crawljax.condition.crawlcondition.CrawlConditionChecker;
import com.crawljax.condition.eventablecondition.EventableConditionChecker;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.condition.invariant.InvariantChecker;
import com.crawljax.core.configuration.CrawlSpecificationReader;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfigurationReader;
import com.crawljax.core.plugin.CrawljaxPluginsUtil;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateFlowGraph;
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
 * @version $Id$
 */
public class CrawljaxController {

	private static final int THOUSAND = 1000;

	private static final Logger LOGGER = Logger.getLogger(CrawljaxController.class.getName());

	private StateVertix indexState;
	private EmbeddedBrowser browser;
	private StateFlowGraph stateFlowGraph;
	private CrawlSession session;

	private long startCrawl;

	private final String propertiesFile;

	private final StateComparator stateComparator;
	private final InvariantChecker invariantChecker = new InvariantChecker();
	private final CrawlConditionChecker crawlConditionChecker = new CrawlConditionChecker();
	private final EventableConditionChecker eventableConditionChecker =
	        new EventableConditionChecker();

	private final WaitConditionChecker waitConditionChecker = new WaitConditionChecker();
	private Crawler crawler;

	private final CrawljaxConfiguration crawljaxConfiguration;

	private final List<OracleComparator> oracleComparator;

	private String lastStateName;

	/**
	 * Central thread starting engine.
	 */
	private final ThreadPoolExecutor workQueue;

	/**
	 * Mutex used for the lock on examinedElements.
	 */
	private final Semaphore examinedElementsMutex = new Semaphore(1);

	/**
	 * Use AtomicInteger to denote the stateCounter, in doing so its thread-safe.
	 */
	private final AtomicInteger stateCounter = new AtomicInteger(1);

	/**
	 * Use the AtomicInteger to prevent Problems when increasing.
	 */
	private final AtomicInteger numberofExaminedElements = new AtomicInteger();

	/**
	 * Use the ConcurrentLinkedQueue to prevent Thread problems when checking and storing
	 * checkedElements.
	 */
	private final Collection<String> checkedElements = new ConcurrentLinkedQueue<String>();

	/**
	 * The constructor.
	 * 
	 * @throws ConfigurationException
	 *             if the configuration fails.
	 */
	public CrawljaxController() throws ConfigurationException {
		this("crawljax.properties");
		LOGGER.warn("No custom setting is provided! Using the default settings.");
	}

	/**
	 * @param propertiesfile
	 *            the properties file.
	 * @throws ConfigurationException
	 *             if the configuration fails.
	 */
	public CrawljaxController(final String propertiesfile) throws ConfigurationException {
		this.propertiesFile = propertiesfile;
		this.crawljaxConfiguration = null;
		this.oracleComparator = new ArrayList<OracleComparator>();
		stateComparator = new StateComparator(this.oracleComparator);
		workQueue = init();
	}

	/**
	 * @param config
	 *            the crawljax configuration.
	 * @throws ConfigurationException
	 *             if the configuration fails.
	 */
	public CrawljaxController(final CrawljaxConfiguration config) throws ConfigurationException {
		this.propertiesFile = null;
		this.crawljaxConfiguration = config;
		CrawljaxConfigurationReader configReader = new CrawljaxConfigurationReader(config);
		CrawlSpecificationReader crawlerReader =
		        new CrawlSpecificationReader(configReader.getCrawlSpecification());
		this.oracleComparator = crawlerReader.getOracleComparators();
		stateComparator = new StateComparator(crawlerReader.getOracleComparators());
		invariantChecker.setInvariants(crawlerReader.getInvariants());
		crawlConditionChecker.setCrawlConditions(crawlerReader.getCrawlConditions());
		waitConditionChecker.setWaitConditions(crawlerReader.getWaitConditions());
		eventableConditionChecker.setEventableConditions(configReader.getEventableConditions());
		workQueue = init();
	}

	/**
	 * @throws ConfigurationException
	 *             if the configuration fails.
	 * @NotThreadSafe
	 */
	private ThreadPoolExecutor init() throws ConfigurationException {
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

		LOGGER.info("Used plugins:");
		CrawljaxPluginsUtil.loadPlugins();

		if (PropertyHelper.getCrawljaxConfiguration() != null) {
			CrawljaxPluginsUtil.runProxyServerPlugins(PropertyHelper.getCrawljaxConfiguration()
			        .getProxyConfiguration());
		}

		LOGGER.info("Embedded browser implementation: " + BrowserFactory.getBrowserTypeString());
		crawler = new Crawler(this);

		HibernateUtil.initialize();

		LOGGER.info("Number of threads: " + PropertyHelper.getCrawNumberOfThreadsValue());

		LOGGER.info("Crawljax initialized!");

		return new ThreadPoolExecutor(PropertyHelper.getCrawNumberOfThreadsValue(),
		        PropertyHelper.getCrawNumberOfThreadsValue(), 0L, TimeUnit.MILLISECONDS,
		        new CrawlQueue());
	}

	/**
	 * Run Crawljax.
	 * 
	 * @throws CrawljaxException
	 *             If the browser cannot be instantiated.
	 * @throws ConfigurationException
	 *             if crawljax configuration fails.
	 * @NotThreadSafe
	 */
	public final void run() throws CrawljaxException, ConfigurationException {

		browser = crawler.getBrowser();

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
		                stateComparator.getStrippedDom(browser));

		// TODO Stefan do something with locking
		stateFlowGraph = new StateFlowGraph();
		stateFlowGraph.requestStateFlowGraphMutex();
		stateFlowGraph.addState(indexState);
		stateFlowGraph.releaseStateFlowGraphMutex();

		// TODO Stefan delete
		StateMachine stateMachine = new StateMachine(stateFlowGraph, indexState);
		crawler.setStateMachine(stateMachine);

		if (crawljaxConfiguration != null) {
			session =
			        new CrawlSession(browser, stateFlowGraph, indexState, crawljaxConfiguration);
		} else {
			session = new CrawlSession(browser, stateFlowGraph, indexState);
		}

		CrawljaxPluginsUtil.runOnNewStatePlugins(session);

		LOGGER
		        .info("Start crawling with " + PropertyHelper.getCrawlTagsValues().size()
		                + " tags");

		try {

			addWorkToQueue(crawler);

			// TODO Stefan it could be possible that a browser is released and a newOne is about to
			// be
			// taken but not ready taken...
			while (!BrowserFactory.isFinished()) {
				try {
					Thread.sleep(THOUSAND);
				} catch (InterruptedException e) {
					LOGGER.error("The waiting on the browsers to be finished was Interruped", e);
				}
			}
		} catch (OutOfMemoryError om) {
			LOGGER.error(om.getMessage(), om);
		}

		long timeCrawlCalc = System.currentTimeMillis() - startCrawl;

		/**
		 * Shutdown the ThreadPool, closing all the possible open Crawler instances
		 */
		this.workQueue.shutdownNow();

		/**
		 * Close all the opened browsers
		 */
		BrowserFactory.close();

		for (Eventable c : stateFlowGraph.getAllEdges()) {
			LOGGER.info("Interaction Element= " + c.toString());
		}

		LOGGER.info("Total Crawling time(" + timeCrawlCalc + "ms) ~= " + formatRunningTime());
		LOGGER.info("EXAMINED ELEMENTS: " + numberofExaminedElements.get());
		LOGGER.info("CLICKABLES: " + stateFlowGraph.getAllEdges().size());
		LOGGER.info("STATES: " + stateFlowGraph.getAllStates().size());
		LOGGER.info("Dom average size (byte): " + stateFlowGraph.getMeanStateStringSize());

		LOGGER.info("Starting PostCrawlingPlugins...");

		CrawljaxPluginsUtil.runPostCrawlingPlugins(session);

		LOGGER.info("DONE!!!");
	}

	/**
	 * Checks the state and time constraints. This function is nearly Thread-safe
	 * 
	 * @return true if all conditions are met.
	 */
	@GuardedBy("stateFlowGraph")
	public boolean checkConstraints() {
		long timePassed = System.currentTimeMillis() - startCrawl;

		if ((PropertyHelper.getCrawlMaxTimeValue() != 0)
		        && (timePassed > PropertyHelper.getCrawlMaxTimeValue())) {

			/* remove all possible candidates left */
			// EXACTEVENTPATH.clear(); TODO Stefan: FIX this!
			LOGGER.info("Max time " + PropertyHelper.getCrawlMaxTimeValue() + "passed!");
			/* stop crawling */
			return false;
		}

		synchronized (stateFlowGraph) {
			if ((PropertyHelper.getCrawlMaxStatesValue() != 0)
			        && (stateFlowGraph.getAllStates().size() >= PropertyHelper
			                .getCrawlMaxStatesValue())) {
				/* remove all possible candidates left */
				// EXACTEVENTPATH.clear(); TODO Stefan: FIX this!

				LOGGER.info("Max number of states " + PropertyHelper.getCrawlMaxStatesValue()
				        + " reached!");

				/* stop crawling */
				return false;
			}
		}
		/* continue crawling */
		return true;
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
	public final boolean isDomChanged(final StateVertix stateBefore, final StateVertix stateAfter) {
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
	 * Return the name of the (new)State. By using the AtomicInteger the stateCounter is thread-safe
	 * 
	 * @return State name the name of the state
	 */
	public final String getNewStateName() {
		stateCounter.getAndIncrement();
		String state = "state" + stateCounter.get();
		lastStateName = state;
		return state;
	}

	/**
	 * Check the invariants. TODO Stefan Check Thread-Safety
	 * 
	 * @param browser
	 *            the browser to feed to the invariants
	 */
	public void checkInvariants(EmbeddedBrowser browser) {
		if (!invariantChecker.check(browser)) {
			final List<Invariant> failedInvariants = invariantChecker.getFailedInvariants();
			for (Invariant failedInvariant : failedInvariants) {
				CrawljaxPluginsUtil.runOnInvriantViolationPlugins(failedInvariant, session);
			}
		}
	}

	/**
	 * @return the eventableConditionChecker
	 * @NotTheadSafe The Condition classes contains 1 not Thread safe implementation
	 *               (XPathCondition)
	 */
	public final EventableConditionChecker getEventableConditionChecker() {
		return eventableConditionChecker;
	}

	/**
	 * @return the oracleComparator
	 * @NotTheadSafe The Condition classes contains 1 not Thread safe implementation
	 *               (XPathCondition)
	 */
	public final List<OracleComparator> getOracleComparator() {
		return oracleComparator;
	}

	/**
	 * Retrieve the current session, there is only one session active at a time. So this method by
	 * it self is Thread-Safe but actions on the session are NOT!
	 * 
	 * @return the session
	 */
	public final CrawlSession getSession() {
		return session;
	}

	/**
	 * Add work (Crawler) to the Queue of work that need to be done. The class is thread-safe.
	 * 
	 * @param work
	 *            the work (Crawler) to add to the Queue
	 */
	@GuardedBy("workQueue")
	public final void addWorkToQueue(Crawler work) {
		synchronized (workQueue) {
			workQueue.execute(work);
		}
	}

	/**
	 * Check if a given element is already checked, preventing duplicate work. This is implemented
	 * in a ConcurrentLinkedQueue to support thread-safety
	 * 
	 * @param element
	 *            the to search for if its already checked
	 * @return true if the element is already checked
	 */
	@GuardedBy("examinedElementsMutex")
	public final boolean elementIsAlreadyChecked(String element) {
		if (examinedElementsMutex.availablePermits() != 0) {
			LOGGER.warn("possible code executing without required permit!", new Throwable(
			        "possible code executing without required permit!").fillInStackTrace());
		}
		return this.checkedElements.contains(element);
	}

	/**
	 * Mark a given element as checked to prevent duplicate work. This is implemented in a
	 * ConcurrentLinkedQueue to support thread-safety
	 * 
	 * @param element
	 *            the elements that is checked
	 */
	@GuardedBy("examinedElementsMutex")
	public final void markElementAsChecked(String element) {
		if (examinedElementsMutex.availablePermits() != 0) {
			LOGGER.warn("possible code executing without required permit!", new Throwable(
			        "possible code executing without required permit!").fillInStackTrace());
		}
		this.checkedElements.add(element);
	}

	/**
	 * Wait for a given condition. This call is thread safe as the underlying object is thread-safe.
	 * 
	 * @param browser
	 *            the browser which requires a wait condition
	 */
	public final void doBrowserWait(EmbeddedBrowser browser) {
		this.waitConditionChecker.wait(browser);
	}

	/**
	 * Retrieve the index state. This class is supposed to be thread safe, but be care full that no
	 * one changes the indexState...
	 * 
	 * @return the indexState of the current crawl
	 */
	public final StateVertix getIndexState() {
		return this.indexState;
	}

	/**
	 * Return the Checker of the CrawlConditions. This call itself is thread-safe but the
	 * crawlconditionCheck is nearly thread-safe, the failedCrawlConditions could cause trouble.
	 * 
	 * @return the crawlConditionChecker
	 */
	public final CrawlConditionChecker getCrawlConditionChecker() {
		return crawlConditionChecker;
	}

	/**
	 * increase the number of checked elements, as a statistics measure to know how many elements
	 * were actually examined. Its thread safe by using the AtomicInteger
	 * 
	 * @see java.util.concurrent.atomic.AtomicInteger
	 */
	public final void increaseNumberExaminedElements() {
		numberofExaminedElements.getAndIncrement();
	}

	/**
	 * get the stripped version of the dom currently in the browser. This call is thread safe, must
	 * be synchronized because there is thread-intefearing bug in the stateComparator.
	 * 
	 * @param browser
	 *            the browser instance.
	 * @return a stripped string of the DOM tree taken from the browser.
	 */
	public synchronized String getStripedDom(EmbeddedBrowser browser) {
		return this.stateComparator.getStrippedDom(browser);
	}

	/**
	 * @return the crawler
	 */
	public final Crawler getCrawler() {
		return crawler;
	}

	private String formatRunningTime() {
		long timeCrawlCalc = System.currentTimeMillis() - startCrawl;
		return String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(timeCrawlCalc),
		        TimeUnit.MILLISECONDS.toSeconds(timeCrawlCalc)
		                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
		                        .toMinutes(timeCrawlCalc)));
	}

	/**
	 * Terminate the crawling, Stop all threads this will cause the controller which is sleeping to
	 * reactive and do the final work....
	 */
	@GuardedBy("this")
	public final synchronized void terminate() {
		LOGGER.warn("After " + this.formatRunningTime()
		        + " the crawling process was requested to terminate @ " + Thread.currentThread());
		LOGGER.info("Trying to stop all the threads");
		// TODO Stefan do the actual termination of all the threads. Also test if it works!
		LOGGER.info("There are " + workQueue.getActiveCount() + " threads active");
		workQueue.shutdownNow();

		if (workQueue.isShutdown()) {
			LOGGER.info("ThreadPoolExecuter is shutdown");
		} else {
			LOGGER.warn("ThreadPoolExecuter is not shutdown");
		}
		if (workQueue.isTerminated()) {
			LOGGER.info("All threads are terminated");
		} else {
			LOGGER.warn("Not All threads are terminated, there still are "
			        + workQueue.getActiveCount() + " threads active");
		}
		LOGGER.info("Trying to close all browsers");
		/**
		 * Needs some more testing when Threads are not finished, the browser gets locked...
		 */
		BrowserFactory.close();
	}

	/**
	 * Return the central data component, the call itself is thread safe. All operations / actions
	 * on the stateFlowGraph should be done very carefully!
	 * 
	 * @return the stateFlowGraph used to store all states and edges
	 */
	public final StateFlowGraph getStateFlowGraph() {
		return stateFlowGraph;
	}

	/**
	 * Request a lock on the examinedElements datastructure. Becarefull a requested lock MUST be
	 * returned by hand! using the {@link #releaseExaminedElementsMutex()}
	 * 
	 * @see CrawljaxController#releaseExaminedElementsMutex()
	 */
	public void requestExaminedElementsMutex() {
		try {
			examinedElementsMutex.acquire();
		} catch (InterruptedException e) {
			LOGGER.error("the acquire of the ExaminedElementsMutex is interuped", e);
		}
	}

	/**
	 * Release the lock for the examinedElements datastructure.
	 */
	public void releaseExaminedElementsMutex() {
		examinedElementsMutex.release();
	}

	/**
	 * Return the last known state name, this call and set operation are not thread safe.
	 * 
	 * @return the lastStateName known in the Controller
	 */
	public final String getLastStateName() {
		return lastStateName;
	}

}
