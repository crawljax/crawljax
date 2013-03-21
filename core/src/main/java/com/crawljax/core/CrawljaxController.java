package com.crawljax.core;

import java.util.concurrent.TimeUnit;

import net.jcip.annotations.GuardedBy;

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.BrowserPool;
import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.ConditionTypeChecker;
import com.crawljax.condition.browserwaiter.WaitConditionChecker;
import com.crawljax.condition.crawlcondition.CrawlCondition;
import com.crawljax.condition.eventablecondition.EventableConditionChecker;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.oraclecomparator.StateComparator;
import com.google.common.collect.ImmutableList;

/**
 * The Crawljax Controller class is the core of Crawljax.
 */
public class CrawljaxController implements CrawlQueueManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(CrawljaxController.class
	        .getName());

	private CrawlSession session;

	private long startCrawl;

	private final StateComparator stateComparator;
	private final ConditionTypeChecker<CrawlCondition> crawlConditionChecker;
	private final EventableConditionChecker eventableConditionChecker;

	private final WaitConditionChecker waitConditionChecker = new WaitConditionChecker();

	// TODO Stefan, Can not be final because, must be created after the loading of the plugins
	private Crawler initialCrawler;

	private final CrawljaxConfiguration configuration;

	private final ImmutableList<Invariant> invariantList;

	/**
	 * Central thread starting engine.
	 */
	private final CrawlerExecutor workQueue;

	private final CandidateElementManager elementChecker;

	private final BrowserPool browserPool;

	/**
	 * @param config
	 *            the crawljax configuration.
	 * @throws ConfigurationException
	 *             if the configuration fails.
	 */
	public CrawljaxController(final CrawljaxConfiguration config) throws CrawljaxException {
		configuration = config;

		stateComparator = new StateComparator(config.getCrawlRules().getOracleComparators());
		invariantList = config.getCrawlRules().getInvariants();

		waitConditionChecker.setWaitConditions(config.getCrawlRules().getPreCrawlConfig()
		        .getWaitConditions());
		eventableConditionChecker =
		        new EventableConditionChecker(config.getCrawlRules());

		crawlConditionChecker =
		        new ConditionTypeChecker<>(config.getCrawlRules().getPreCrawlConfig()
		                .getCrawlConditions());
		elementChecker =
		        new CandidateElementManager(eventableConditionChecker, crawlConditionChecker);

		browserPool = new BrowserPool(config);

		workQueue = init();
	}

	/**
	 * @throws ConfigurationException
	 *             if the configuration fails.
	 * @NotThreadSafe
	 */
	private CrawlerExecutor init() {
		LOGGER.info("Starting Crawljax...");

		LOGGER.info("Used plugins:");

		if (configuration.getProxyConfiguration() != null) {
			configuration.getPlugins().runProxyServerPlugins(
			        configuration.getProxyConfiguration());
		}

		LOGGER.info("Embedded browser implementation: {}", configuration.getBrowserConfig()
		        .getBrowsertype());

		LOGGER.info("Crawl depth: {}", configuration.getMaximumDepth());
		LOGGER.info("Crawljax initialized!");

		return new CrawlerExecutor(configuration.getBrowserConfig());
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
	public final void run() throws CrawljaxException {

		startCrawl = System.currentTimeMillis();

		LOGGER.info("Start crawling with {} crawl elements", configuration.getCrawlRules()
		        .getPreCrawlConfig().getIncludedElements());

		// Create the initailCrawler
		initialCrawler = new InitialCrawler(this, configuration.getPlugins());

		// Start the Crawling by adding the initialCrawler to the the workQueue.
		addWorkToQueue(initialCrawler);

		try {
			// Block until the all the jobs are done
			workQueue.waitForTermination();
		} catch (InterruptedException e) {
			LOGGER.error(e.getMessage(), e);
		}

		if (workQueue.isAborted()) {
			LOGGER.warn("It apears to be that the workQueue was Aborted, "
			        + "not running postcrawling plugins and not closing the browsers");
			return;
		}

		long timeCrawlCalc = System.currentTimeMillis() - startCrawl;

		/**
		 * Close all the opened browsers, this is run in separate thread to have the post crawl
		 * plugins to execute in the meanwhile.
		 */
		Thread shutdownThread = browserPool.close();

		// TODO Stefan; Now we "re-request" a browser instance for the PostCrawlingPlugins Thread,
		// this is not ideal...
		EmbeddedBrowser b = null;
		try {
			b = this.getBrowserPool().requestBrowser();
		} catch (InterruptedException e1) {
			LOGGER.warn("Re-Request for a browser was interrupted", e1);
		}
		configuration.getPlugins().runPostCrawlingPlugins(session);
		this.getBrowserPool().freeBrowser(b);

		this.shutdown(timeCrawlCalc);

		try {
			shutdownThread.join();
		} catch (InterruptedException e) {
			LOGGER.error("could not wait for browsers to close.", e);
		}

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
	@Override
	public final void addWorkToQueue(Crawler work) {
		workQueue.execute(work);
	}

	/**
	 * Removes this Crawler from the workQueue if it is present, thus causing it not to be run if it
	 * has not already started.
	 * 
	 * @param crawler
	 *            the Crawler to remove
	 * @return true if the crawler was removed
	 */
	@Override
	public boolean removeWorkFromQueue(Crawler crawler) {
		return workQueue.remove(crawler);
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
	 * TODO Stefan: Remove this synchronization; performance loss is huge! no synchrnization fails
	 * because ThreadLocal is not ThreadSafe??? get the stripped version of the dom currently in the
	 * browser. This call is thread safe, must be synchronised because there is thread-intefearing
	 * bug in the stateComparator.
	 * 
	 * @param browser
	 *            the browser instance.
	 * @return a stripped string of the DOM tree taken from the browser.
	 */
	public synchronized String getStrippedDom(EmbeddedBrowser browser) {
		return this.stateComparator.getStrippedDom(browser);
	}

	/**
	 * @deprecated use the {@link #getInitialCrawler()} instead, does exactly the same.
	 * @return the crawler used to initiate the Crawling run.
	 */
	@Deprecated
	public final Crawler getCrawler() {
		return getInitialCrawler();
	}

	/**
	 * Retrieve the initial Crawler used.
	 * 
	 * @return the initialCrawler used to initiate the Crawling run.
	 */
	public final Crawler getInitialCrawler() {
		return initialCrawler;
	}

	/**
	 * Format the time the current crawl run has taken into a more readable format. Taking now as
	 * the end time of the crawling.
	 * 
	 * @return the formatted time in X min, X sec layout.
	 */
	private String formatRunningTime() {
		return formatRunningTime(System.currentTimeMillis() - startCrawl);
	}

	/**
	 * Format the time the current crawl run has taken into a more readable format.
	 * 
	 * @param timeCrawlCalc
	 *            the time to display
	 * @return the formatted time in X min, X sec layout.
	 */
	private String formatRunningTime(long timeCrawlCalc) {
		return String.format(
		        "%d min, %d sec",
		        TimeUnit.MILLISECONDS.toMinutes(timeCrawlCalc),
		        TimeUnit.MILLISECONDS.toSeconds(timeCrawlCalc)
		                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
		                        .toMinutes(timeCrawlCalc)));
	}

	/**
	 * Terminate the crawling, Stop all threads this will cause the controller which is sleeping to
	 * reactive and do the final work....
	 * 
	 * @param isAbort
	 *            if set true the terminate must be as an abort not allowing running PostCrawling
	 *            plugins.
	 */
	@Override
	@GuardedBy("this")
	public final synchronized void terminate(boolean isAbort) {
		LOGGER.warn("After " + this.formatRunningTime()
		        + " the crawling process was requested to terminate @ " + Thread.currentThread());
		browserPool.shutdown();
		workQueue.shutdownNow(isAbort);
		this.shutdown(System.currentTimeMillis() - startCrawl);
	}

	/**
	 * The general shutdown procedure without running plugins or using browsers.
	 */
	private void shutdown(long timeCrawlCalc) {
		StateFlowGraph stateFlowGraph = this.getSession().getStateFlowGraph();
		for (Eventable c : stateFlowGraph.getAllEdges()) {
			LOGGER.info("Interaction Element= " + c.toString());
		}
		LOGGER.info("Total Crawling time(" + timeCrawlCalc + "ms) ~= "
		        + formatRunningTime(timeCrawlCalc));
		LOGGER.info("EXAMINED ELEMENTS: " + elementChecker.numberOfExaminedElements());
		LOGGER.info("CLICKABLES: " + stateFlowGraph.getAllEdges().size());
		LOGGER.info("STATES: " + stateFlowGraph.getAllStates().size());
		LOGGER.info("Dom average size (byte): " + stateFlowGraph.getMeanStateStringSize());
		LOGGER.info("DONE!!!");
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
	 * @return the configurationReader
	 */
	public CrawljaxConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * @return the browser pool.
	 */
	public BrowserPool getBrowserPool() {
		return browserPool;
	}

	/**
	 * Return the used CrawlQueueManager, this method is designed for extension purposes. Being able
	 * to move the {@link #addWorkToQueue(Crawler)} and {@link #removeWorkFromQueue(Crawler)} out of
	 * this class using the interface.
	 * 
	 * @return the crawlQueueManager that is used.
	 */
	public CrawlQueueManager getCrawlQueueManager() {
		return this;
	}

	/**
	 * @return the invariantList
	 */
	public final ImmutableList<Invariant> getInvariantList() {
		return invariantList;
	}

	/**
	 * Install a new CrawlSession.
	 * 
	 * @param session
	 *            set the new value for the session
	 */
	public void setSession(CrawlSession session) {
		this.session = session;
	}

	/**
	 * @return the startCrawl
	 */
	public final long getStartCrawl() {
		return startCrawl;
	}

	@Override
	public void waitForTermination() throws InterruptedException {
		this.workQueue.waitForTermination();
	}

}
