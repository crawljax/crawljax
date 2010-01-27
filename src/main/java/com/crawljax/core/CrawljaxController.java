package com.crawljax.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.jcip.annotations.GuardedBy;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import com.crawljax.browser.BrowserFactory;
import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.browserwaiter.WaitConditionChecker;
import com.crawljax.condition.crawlcondition.CrawlConditionChecker;
import com.crawljax.condition.eventablecondition.EventableConditionChecker;
import com.crawljax.condition.invariant.Invariant;
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

	private static final int TERMINATIONWAITTIMEOUT = 1000;

	private static final Logger LOGGER = Logger.getLogger(CrawljaxController.class.getName());

	private StateVertix indexState;
	private EmbeddedBrowser browser;

	private StateFlowGraph stateFlowGraph;
	private CrawlSession session;

	private long startCrawl;

	private final String propertiesFile;

	private final StateComparator stateComparator;
	private final CrawlConditionChecker crawlConditionChecker = new CrawlConditionChecker();
	private final EventableConditionChecker eventableConditionChecker =
	        new EventableConditionChecker();

	private final WaitConditionChecker waitConditionChecker = new WaitConditionChecker();
	private Crawler crawler;

	private final CrawljaxConfiguration crawljaxConfiguration;

	private final List<Invariant> invariantList;

	/**
	 * Central thread starting engine.
	 */
	private final ThreadPoolExecutor workQueue;

	private final CandidateElementManager elementChecker =
	        new CandidateElementManager(eventableConditionChecker, crawlConditionChecker);

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
		stateComparator = new StateComparator(new ArrayList<OracleComparator>());
		invariantList = new ArrayList<Invariant>();
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

		stateComparator = new StateComparator(crawlerReader.getOracleComparators());
		invariantList = crawlerReader.getInvariants();
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
		LOGGER.info("Crawl depth: " + PropertyHelper.getCrawlDepthValue());
		LOGGER.info("Crawljax initialized!");

		return new ThreadPoolExecutor(PropertyHelper.getCrawNumberOfThreadsValue(),
		        PropertyHelper.getCrawNumberOfThreadsValue(), 0L, TimeUnit.MILLISECONDS,
		        new CrawlQueue(), new CrawlThreadFactory());
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

		stateFlowGraph = new StateFlowGraph(indexState);

		StateMachine stateMachine = new StateMachine(stateFlowGraph, indexState, invariantList);
		crawler.setStateMachine(stateMachine);

		if (crawljaxConfiguration != null) {
			session =
			        new CrawlSession(browser, stateFlowGraph, indexState, startCrawl,
			                crawljaxConfiguration);
		} else {
			session = new CrawlSession(browser, stateFlowGraph, indexState, startCrawl);
		}

		CrawljaxPluginsUtil.runOnNewStatePlugins(session);

		LOGGER
		        .info("Start crawling with " + PropertyHelper.getCrawlTagsValues().size()
		                + " tags");

		try {

			addWorkToQueue(crawler);

			// TODO Stefan it could be possible that a browser is released and a newOne is about to
			// be taken but not ready taken...
			while (!BrowserFactory.isFinished()) {
				try {
					Thread.sleep(TERMINATIONWAITTIMEOUT);
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
		LOGGER.info("EXAMINED ELEMENTS: " + elementChecker.numberOfExaminedElements());
		LOGGER.info("CLICKABLES: " + stateFlowGraph.getAllEdges().size());
		LOGGER.info("STATES: " + stateFlowGraph.getAllStates().size());
		LOGGER.info("Dom average size (byte): " + stateFlowGraph.getMeanStateStringSize());

		LOGGER.info("Starting PostCrawlingPlugins...");

		CrawljaxPluginsUtil.runPostCrawlingPlugins(session);

		LOGGER.info("DONE!!!");
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
	 * Wait for a given condition. This call is thread safe as the underlying object is thread-safe.
	 * 
	 * @param browser
	 *            the browser which requires a wait condition
	 */
	public final void doBrowserWait(EmbeddedBrowser browser) {
		this.waitConditionChecker.wait(browser);
	}

	/**
	 * get the stripped version of the dom currently in the browser. This call is thread safe, must
	 * be synchronised because there is thread-intefearing bug in the stateComparator.
	 * 
	 * @param browser
	 *            the browser instance.
	 * @return a stripped string of the DOM tree taken from the browser.
	 */
	@GuardedBy("this")
	public synchronized String getStripedDom(EmbeddedBrowser browser) {
		return this.stateComparator.getStrippedDom(browser);
	}

	/**
	 * @return the crawler
	 */
	public final Crawler getCrawler() {
		return crawler;
	}

	/**
	 * Format the time the current crawl run has taken into a more readable format.
	 * 
	 * @return the formatted time in X min, X sec layout.
	 */
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
	 * The current element checker in use. This call is thread-safe because it returns a final
	 * field.
	 * 
	 * @return the elementChecker used to register the checked elements.
	 */
	public final ExtractorManager getElementChecker() {
		return elementChecker;
	}

	/**
	 * Make a new StateMachine supplied with the data in this controller.
	 * 
	 * @return a new StateMachine.
	 */
	public StateMachine buildNewStateMachine() {
		return new StateMachine(this.stateFlowGraph, this.indexState, this.invariantList);
	}

}
