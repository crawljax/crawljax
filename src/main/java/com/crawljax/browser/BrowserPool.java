/**
 * Created Jan 18, 2008
 */
package com.crawljax.browser;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.CrawlQueueManager;
import com.crawljax.core.configuration.CrawljaxConfigurationReader;
import com.crawljax.core.configuration.ThreadConfigurationReader;
import com.crawljax.core.plugin.CrawljaxPluginsUtil;

/**
 * The Pool class returns an instance of the desired browser as specified in the properties file.
 * 
 * @author mesbah
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id$
 */
public final class BrowserPool {
	private static final Logger LOGGER = LoggerFactory.getLogger(BrowserPool.class);

	/**
	 * BlockingQueue used to block for the moment when a browser comes available.
	 */
	private final BlockingQueue<EmbeddedBrowser> available;

	/**
	 * ConcurrentLinkedQueue used to store the taken browsers.
	 */
	private final ConcurrentLinkedQueue<EmbeddedBrowser> taken =
	        new ConcurrentLinkedQueue<EmbeddedBrowser>();

	/**
	 * Boot class used to handle and monitor the boot process.
	 */
	private final BrowserBooter booter;

	/**
	 * The amount of time used to wait between every check for the close operation.
	 */
	private final int shutdownTimeout = 100;

	private final ThreadConfigurationReader threadConfig;

	private int retries = 0;

	private final AtomicInteger activeBrowserCount = new AtomicInteger(0);

	private ThreadLocal<EmbeddedBrowser> currentBrowser = new ThreadLocal<EmbeddedBrowser>();

	private final Semaphore preCrawlingBlocker = new Semaphore(0);

	private final AtomicBoolean preCrawlingRun = new AtomicBoolean(false);

	private final EmbeddedBrowserBuilder builder;

	private final CrawljaxConfigurationReader configuration;

	/* Config values */

	/**
	 * Is the browser booting in use?
	 * 
	 * @return true if the browser booting is in use.
	 */
	private boolean useBooting() {
		return threadConfig.isBrowserBooting();
	}

	/**
	 * Get the number of Browsers from the config.
	 * 
	 * @return the number of browsers.
	 */
	private int getNumberOfBrowsers() {
		return threadConfig.getNumberBrowsers();
	}

	/**
	 * @return the number of retries a creation of browser can have.
	 * @see com.crawljax.core.configuration.ThreadConfigurationReader#
	 *      getNumberBrowserCreateRetries()
	 */
	private int getNumberBrowserCreateRetries() {
		return threadConfig.getNumberBrowserCreateRetries();
	}

	/**
	 * @return the time in milliseconds to sleep after a browser creation failure.
	 * @see com.crawljax.core.configuration.ThreadConfigurationReader#
	 *      getSleepTimeOnBrowserCreationFailure()
	 */
	private int getSleepTimeOnBrowserCreationFailure() {
		return threadConfig.getSleepTimeOnBrowserCreationFailure();
	}

	/**
	 * The default constructor for the BrowserPool. It's feeded with a configurationReader to read
	 * all the configuration settings.
	 * 
	 * @param configurationReader
	 *            the configurationReader used to read the configuration options from.
	 */
	public BrowserPool(CrawljaxConfigurationReader configurationReader) {
		this.configuration = configurationReader;
		this.threadConfig = configurationReader.getThreadConfigurationReader();
		this.builder = configurationReader.getBrowserBuilder();
		this.available =
		        new ArrayBlockingQueue<EmbeddedBrowser>(threadConfig.getNumberBrowsers(), true);
		this.booter = new BrowserBooter(this);
	}

	/**
	 * Internal used to requestBrowser.
	 * 
	 * @see #requestBrowser()
	 * @return the new browser TODO Stefan; this is exposing the WebDriver API
	 * @throws WebDriverException
	 *             a WebDriverException is thrown by the WebDriver when creation of the browser
	 *             failed.
	 */
	private EmbeddedBrowser createBrowser() {
		EmbeddedBrowser newBrowser = getBrowserInstance();

		if (taken.size() == 0 && available.size() == 0
		        && preCrawlingRun.compareAndSet(false, true)) {
			// this is the first browser && no preCrawling has run or is running
			// We are the one that will run the preCrawling plugins
			// preCrawlingRun.compareAndSet(false, true) equals to !preCrawlingRun ->
			// preCrawlingRun = true

			/**
			 * Start by running the PreCrawlingPlugins
			 */
			CrawljaxPluginsUtil.runPreCrawlingPlugins(newBrowser);

			// Release the blocker for the total amount of browsers -1 (because this one does not
			// have to block) anymore
			preCrawlingBlocker.release(this.getNumberOfBrowsers() - 1);
		} else {
			// Block until the PreCrawling phase has been executed
			try {
				preCrawlingBlocker.acquire();
			} catch (InterruptedException e) {
				LOGGER.error("Waiting for the preCrawlingPlugins"
				        + " to execute first has been interupped, "
				        + "continuing with the OnBrowserCreatedPlugins", e);
			}
		}
		/**
		 * Start by running the OnBrowserCreatedPlugins
		 */
		CrawljaxPluginsUtil.runOnBrowserCreatedPlugins(newBrowser);

		assert (newBrowser != null);
		return newBrowser;

	}

	/**
	 * Depended on the {@link EmbeddedBrowser.BrowserType} a new instance is made.
	 * 
	 * @return the new Object holding the EmbeddedBrowser instance. TODO Stefan; this is exposing
	 *         the WebDriver API
	 * @throws WebDriverException
	 *             a WebDriverException is thrown by the WebDriver when creation of the browser
	 *             failed.
	 */
	private EmbeddedBrowser getBrowserInstance() {
		EmbeddedBrowser embeddedBrowser = builder.buildEmbeddedBrowser(configuration);
		embeddedBrowser.updateConfiguration(configuration);
		return embeddedBrowser;
	}

	/**
	 * Close all browser windows. in a Separate Thread
	 * 
	 * @return the Thread currently executing the shutdown.
	 */
	public synchronized Thread close() {
		Thread closeBrowsers = new Thread(new Runnable() {
			@Override
			public void run() {
				Queue<EmbeddedBrowser> deleteList = new LinkedList<EmbeddedBrowser>();
				if (useBooting()) {
					booter.shutdown();
				}
				for (EmbeddedBrowser b : available) {
					try {
						b.close();
					} finally {
						deleteList.add(b);
					}
				}
				available.removeAll(deleteList);
				deleteList = new LinkedList<EmbeddedBrowser>();
				for (EmbeddedBrowser b : taken) {
					try {
						b.close();
					} finally {
						deleteList.add(b);
					}
				}
				taken.removeAll(deleteList);
				currentBrowser = new ThreadLocal<EmbeddedBrowser>();
				assert (available.isEmpty());
				assert (taken.isEmpty());
			}
		});
		closeBrowsers.setName("Browser closing Thread");
		closeBrowsers.start();
		return closeBrowsers;
	}

	/**
	 * Return the browser, release the current browser for further operations. This method is
	 * Synchronised to prevent problems in combination with isFinished.
	 * 
	 * @param browser
	 *            the browser which is not needed anymore
	 */
	public synchronized void freeBrowser(EmbeddedBrowser browser) {
		assert (browser != null);
		taken.remove(browser);
		available.add(browser);
		currentBrowser.remove();
	}

	/**
	 * Place a request for a browser.
	 * 
	 * @return a new Browser instance which is currently free
	 * @throws InterruptedException
	 *             the InterruptedException is thrown when the AVAILABE list is interrupted.
	 */
	public EmbeddedBrowser requestBrowser() throws InterruptedException {
		EmbeddedBrowser browser = null;
		if (useBooting()) {
			booter.start();
			browser = waitForBrowser();
		} else {
			if (available.size() > 0) {
				// There are browsers available
				browser = available.take();
				taken.add(browser);
			} else if (activeBrowserCount.getAndIncrement() < getNumberOfBrowsers()) {
				// We are not at the limit of the number of browsers so create one
				try {
					browser = createBrowser();
				} catch (WebDriverException e) {
					// TODO Stefan; this is exposing the WebDriver API
					LOGGER.error("Faild to create a browser!", e);
					if (getNumberBrowserCreateRetries() > 0
					        && retries < getNumberBrowserCreateRetries()) {
						retries++;

						// Do we need to sleep after a crash?
						if (getSleepTimeOnBrowserCreationFailure() > 0) {
							Thread.sleep(getSleepTimeOnBrowserCreationFailure());
						}

						browser = requestBrowser();
					}
					if (browser == null) {
						activeBrowserCount.decrementAndGet();
						LOGGER.error("I could (might) not rescue a browser creation!", e);
						throw e;
					}
				}
				taken.add(browser);
			} else {
				// The max number of browsers has been made, so wait for a browser to become
				// available.
				browser = waitForBrowser();
			}
		}
		assert (browser != null);
		assert (taken.contains(browser));
		currentBrowser.set(browser);
		return browser;
	}

	/**
	 * This call blocks until a browser comes available.
	 * 
	 * @return the browser currently reserved.
	 * @throws InterruptedException
	 *             when available.take is Interrupted
	 */
	private EmbeddedBrowser waitForBrowser() throws InterruptedException {
		EmbeddedBrowser b = available.take();
		assert (b != null);
		taken.add(b);
		return b;
	}

	/**
	 * Are all takenBrowsers free (again)? this method is Synchronised to prevent problems in
	 * combination with freeBrowser.
	 * 
	 * @return true if the taken list of Browsers is empty
	 */
	public synchronized boolean isFinished() {

		return taken.isEmpty();
	}

	/**
	 * This private class is used to boot all the browsers.
	 * 
	 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
	 */
	private class BrowserBooter extends Thread {
		private boolean finished = false;
		private final AtomicBoolean started;
		private final AtomicInteger createdBrowserCount;
		private final AtomicInteger failedCreatedBrowserCount;
		private final BrowserPool pool;

		public BrowserBooter(BrowserPool pool) {
			assert (pool != null);
			this.pool = pool;
			started = new AtomicBoolean(false);
			createdBrowserCount = new AtomicInteger(0);
			failedCreatedBrowserCount = new AtomicInteger(0);
		}

		@Override
		public void run() {
			int i = 0;

			createdBrowserCount.set(i);

			assert (createdBrowserCount.get() <= 1);

			// Loop to the requested number of browsers
			for (; i < pool.getNumberOfBrowsers(); i++) {
				// Create a new Thread
				new Thread(new Runnable() {
					private int bootRetries = 0;

					@Override
					public void run() {
						try {
							pool.available.add(pool.createBrowser());
							createdBrowserCount.incrementAndGet();
						} catch (Throwable e) {
							/* Catch ALL exceptions... */
							LOGGER.error("Creation of Browser faild!", e);
							if (pool.getNumberBrowserCreateRetries() > 0
							        && bootRetries < pool.getNumberBrowserCreateRetries()) {
								bootRetries++;

								// Do we need to sleep after a crash?
								if (pool.getSleepTimeOnBrowserCreationFailure() > 0) {
									try {
										Thread.sleep(getSleepTimeOnBrowserCreationFailure());
									} catch (InterruptedException e1) {
										LOGGER.error("Interruped while sleepting "
										        + "timeout before retry of "
										        + "creation of new browser instance", e1);
									}
								}
								run();
							} else {
								failedCreatedBrowserCount.incrementAndGet();
								LOGGER.error("Could not rescue browser creation!", e);
							}
						}
					}
				}).start();
			}
			finished = true;
		}

		/**
		 * Shut down the booter, this not really shutsdown the booter but wait for all child Threads
		 * to start.
		 */
		private void shutdown() {
			if (allBrowsersLoaded() || !started.get()) {
				return;
			}
			LOGGER.warn("Waiting for all browsers to be started fully"
			        + " before starting to close them. Created browsers "
			        + createdBrowserCount.get() + " configed browsers "
			        + pool.getNumberOfBrowsers());
			while (!allBrowsersLoaded()) {
				try {
					Thread.sleep(pool.shutdownTimeout);
				} catch (InterruptedException e) {
					LOGGER.error("Closing of the browsers faild due to an Interrupt", e);
				}
			}
		}

		/**
		 * Start this Thread, the Thread will only be started when it is called the first time.
		 */
		@Override
		public void start() {
			if (!finished && started.compareAndSet(false, true)) {
				super.start();
			}
			assert (started.get());
		}

		/**
		 * @return true is all requested browsers are loaded or at least there was an attempt for!.
		 */
		private boolean allBrowsersLoaded() {
			return (failedCreatedBrowserCount.get() + createdBrowserCount.get()) >= pool
			        .getNumberOfBrowsers();
		}
	}

	/**
	 * Returns the browser associated with this Thread.
	 * 
	 * @return the browser associated with this Thread null is non is associated.
	 */
	public EmbeddedBrowser getCurrentBrowser() {
		return this.currentBrowser.get();
	}

	/**
	 * Shutdown the BrowserPool, releasing all taken browsers, closing them and wait untill all
	 * browsers are closed.
	 */
	public void shutdown() {
		try {
			close().join();
		} catch (InterruptedException e) {
			LOGGER.error("The shutdown thread of the BrowserPool was Interrupted");
		}
	}

	/**
	 * Remove a browser from the pool, do not release it again as it might be faulty. A
	 * {@link RuntimeException} might be thrown when there are no more browsers left in the pool
	 * after removing this instance stopping all processing.
	 * 
	 * @param browser
	 *            the browser instance to be remove (not null)
	 * @param terminationHandler
	 *            the call-back handler to call the terminate function on.
	 */
	public synchronized void removeBrowser(EmbeddedBrowser browser,
	        CrawlQueueManager terminationHandler) {
		assert (browser != null);

		// remove from pool
		taken.remove(browser);
		currentBrowser.remove();

		// check if this was the last browser standing if so throw RuntimeException
		if (taken.size() == 0 && available.size() == 0) {
			terminationHandler.terminate(true);
			throw new RuntimeException("All browsers have died; "
			        + "there are no browsers left in the pool to execute Crawlers on!");
		}
	}
}
