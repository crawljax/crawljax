package com.crawljax.core;

import java.util.List;
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
import com.crawljax.oraclecomparator.StateComparator;
import com.crawljax.util.database.HibernateUtil;

/**
 * The Crawljax Controller class is the core of Crawljax.
 * 
 * @author mesbah
 * @version $Id$
 */
public class CrawljaxController {

	private static final Logger LOGGER = Logger.getLogger(CrawljaxController.class.getName());

	private StateVertix indexState;
	private EmbeddedBrowser browser;

	private StateFlowGraph stateFlowGraph;
	private CrawlSession session;

	private long startCrawl;

	private final StateComparator stateComparator;
	private final CrawlConditionChecker crawlConditionChecker = new CrawlConditionChecker();
	private final EventableConditionChecker eventableConditionChecker =
	        new EventableConditionChecker();

	private final WaitConditionChecker waitConditionChecker = new WaitConditionChecker();
	private Crawler crawler;

	private final CrawljaxConfiguration crawljaxConfiguration;
	private final CrawljaxConfigurationReader configurationReader;

	private final List<Invariant> invariantList;

	/**
	 * Central thread starting engine.
	 */
	private final CrawlerExecutor workQueue;

	private final CandidateElementManager elementChecker =
	        new CandidateElementManager(eventableConditionChecker, crawlConditionChecker);

	private final BrowserFactory browserFactory;

	/**
	 * @param config
	 *            the crawljax configuration.
	 * @throws ConfigurationException
	 *             if the configuration fails.
	 */
	public CrawljaxController(final CrawljaxConfiguration config) throws ConfigurationException {
		this.crawljaxConfiguration = config;
		configurationReader = new CrawljaxConfigurationReader(config);
		CrawlSpecificationReader crawlerReader =
		        configurationReader.getCrawlSpecificationReader();

		stateComparator = new StateComparator(crawlerReader.getOracleComparators());
		invariantList = crawlerReader.getInvariants();
		crawlConditionChecker.setCrawlConditions(crawlerReader.getCrawlConditions());
		waitConditionChecker.setWaitConditions(crawlerReader.getWaitConditions());
		eventableConditionChecker.setEventableConditions(configurationReader
		        .getEventableConditions());

		browserFactory =
		        new BrowserFactory(
		                configurationReader.getBrowser(),
		                configurationReader.getThreadConfigurationReader(),
		                configurationReader.getProxyConfiguration(),
		                configurationReader.getFilterAttributeNames(),
		                configurationReader.getCrawlSpecificationReader().getWaitAfterReloadUrl(),
		                configurationReader.getCrawlSpecificationReader().getWaitAfterEvent());

		workQueue = init();
	}

	/**
	 * @throws ConfigurationException
	 *             if the configuration fails.
	 * @NotThreadSafe
	 */
	private CrawlerExecutor init() throws ConfigurationException {
		LOGGER.info("Starting Crawljax...");

		LOGGER.info("Used plugins:");
		CrawljaxPluginsUtil.loadPlugins(configurationReader.getPlugins());

		if (configurationReader.getProxyConfiguration() != null) {
			CrawljaxPluginsUtil
			        .runProxyServerPlugins(configurationReader.getProxyConfiguration());
		}

		LOGGER.info("Embedded browser implementation: " + browserFactory.getBrowserType());

		crawler = new Crawler(this);

		HibernateUtil.initialize(configurationReader.getHibernateConfiguration());

		LOGGER.info("Number of threads: "
		        + configurationReader.getThreadConfigurationReader().getNumberThreads());
		LOGGER.info("Crawl depth: "
		        + configurationReader.getCrawlSpecificationReader().getDepth());
		LOGGER.info("Crawljax initialized!");

		return new CrawlerExecutor(configurationReader.getThreadConfigurationReader()
		        .getNumberThreads());
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
			                new CrawljaxConfigurationReader(crawljaxConfiguration));
		} else {
			session = new CrawlSession(browser, stateFlowGraph, indexState, startCrawl);
		}

		CrawljaxPluginsUtil.runOnNewStatePlugins(session);

		LOGGER.info("Start crawling with "
		        + configurationReader.getAllIncludedCrawlElements().size() + " crawl elements");

		addWorkToQueue(crawler);

		try {
			// Block until the all the jobs are done
			workQueue.waitForTermination();
		} catch (InterruptedException e) {
			LOGGER.error(e.getMessage(), e);
		}

		long timeCrawlCalc = System.currentTimeMillis() - startCrawl;

		/**
		 * Close all the opened browsers
		 */
		browserFactory.close();

		for (Eventable c : stateFlowGraph.getAllEdges()) {
			LOGGER.info("Interaction Element= " + c.toString());
		}

		LOGGER.info("Total Crawling time(" + timeCrawlCalc + "ms) ~= " + formatRunningTime());
		LOGGER.info("EXAMINED ELEMENTS: " + elementChecker.numberOfExaminedElements());
		LOGGER.info("CLICKABLES: " + stateFlowGraph.getAllEdges().size());
		LOGGER.info("STATES: " + stateFlowGraph.getAllStates().size());
		LOGGER.info("Dom average size (byte): " + stateFlowGraph.getMeanStateStringSize());

		CrawljaxPluginsUtil.runPostCrawlingPlugins(session);

		LOGGER.info("DONE!!!");
	}

	/**
	 * Retrieve the current session, there is only one session active at a time. So this method by
	 * it self is Thread-Safe but actions on the session are NOT!
	 * 
	 * @return the session
	 */
	public CrawlSession getSession() {
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
	public synchronized String getStrippedDom(EmbeddedBrowser browser) {
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
		 * TODO: Needs some more testing when Threads are not finished, the browser gets locked...
		 */
		browserFactory.close();
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

	/**
	 * @return the configurationReader
	 */
	public CrawljaxConfigurationReader getConfigurationReader() {
		return configurationReader;
	}

	/**
	 * @return the browser factory.
	 */
	public BrowserFactory getBrowserFactory() {
		return browserFactory;
	}

}
