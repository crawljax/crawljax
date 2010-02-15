/**
 * Created Jan 18, 2008
 */
package com.crawljax.browser;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.firefox.FirefoxProfile;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.ProxyConfiguration;
import com.crawljax.core.configuration.ThreadConfigurationReader;

/**
 * The factory class returns an instance of the desired browser as specified in the properties file.
 * 
 * @author mesbah
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id$
 */
public final class BrowserFactory {
	private static final Logger LOGGER = Logger.getLogger(BrowserFactory.class);

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

	private final BrowserType browserType;

	private final ProxyConfiguration proxyConfiguration;

	private final long crawlWaitReload;

	private final List<String> filterAttributes;

	private final long crawlWaitEvent;

	private final ThreadConfigurationReader threadConfig;

	private int retries = 0;

	private final AtomicInteger activeBrowserCount = new AtomicInteger(0);

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
	 * @return the browser type.
	 */
	public BrowserType getBrowserType() {
		return browserType;
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
	 * Use Fast booting?
	 * 
	 * @return true if fast booting of browsers is enabled.
	 */
	private boolean useFastBooting() {
		return threadConfig.getUseFastBooting();
	}

	/**
	 * Retrieve / generate the port number to use.
	 * 
	 * @return the port number that must be used.
	 */
	private int getPortNumber() {
		return threadConfig.getPortNumber();
	}

	/**
	 * hidden constructor.
	 * 
	 * @param type
	 *            the browser type.
	 * @param threadConfig
	 *            the config holder for thread config values.
	 * @param proxyConfig
	 *            the proxy configuration (can be null).
	 * @param filterAttributes
	 *            the attributes to be filtered from DOM.
	 * @param crawlWaitReload
	 *            the period to wait after a reload.
	 * @param crawlWaitEvent
	 *            the period to wait after an event is fired.
	 */
	public BrowserFactory(BrowserType type, ThreadConfigurationReader threadConfig,
	        ProxyConfiguration proxyConfig, List<String> filterAttributes, long crawlWaitReload,
	        long crawlWaitEvent) {

		this.available =
		        new ArrayBlockingQueue<EmbeddedBrowser>(threadConfig.getNumberBrowsers(), true);
		this.browserType = type;
		this.proxyConfiguration = proxyConfig;
		this.threadConfig = threadConfig;
		this.filterAttributes = filterAttributes;
		this.crawlWaitReload = crawlWaitReload;
		this.crawlWaitEvent = crawlWaitEvent;

		booter = new BrowserBooter(this);

		assert (!booter.isAlive());

	}

	/**
	 * Internal used to requestBrowser.
	 * 
	 * @see {@link #requestBrowser()}
	 * @return the new browser
	 * @throws WebDriverException
	 *             a WebDriverException is thrown by the WebDriver when creation of the browser
	 *             failed.
	 */
	private EmbeddedBrowser createBrowser() {

		if (available.isEmpty() && taken.isEmpty()) {
			// This should be the first browser? use the one from the config
			// More are 'cloned' later....
			return getBrowserInstance();

		}

		EmbeddedBrowser newBrowser = null;
		EmbeddedBrowser currentBrowser = available.element();

		if (currentBrowser != null) {
			// Clone the Browser
			newBrowser = currentBrowser.clone();
		} else {
			// There is no browser specified so try to find the class to use and instance it
			newBrowser = getBrowserInstance();
		}
		assert (newBrowser != null);
		return newBrowser;

	}

	/**
	 * Depended on the {@link #browserType} a new instance is made.
	 * 
	 * @return the new Object holding the EmbeddedBrowser instance.
	 * @throws WebDriverException
	 *             a WebDriverException is thrown by the WebDriver when creation of the browser
	 *             failed.
	 */
	private EmbeddedBrowser getBrowserInstance() {

		switch (browserType) {
			case firefox:
				if (proxyConfiguration != null) {
					return new WebDriverFirefox(proxyConfiguration, this.filterAttributes,
					        this.crawlWaitReload, this.crawlWaitEvent);
				}

				if (useFastBooting()) {
					FirefoxProfile fp = new FirefoxProfile();
					fp.setPort(getPortNumber());
					return new WebDriverFirefox(fp, this.filterAttributes, this.crawlWaitReload,
					        this.crawlWaitEvent);
				}

				return new WebDriverFirefox(this.filterAttributes, this.crawlWaitReload,
				        this.crawlWaitEvent);

			case ie:
				return new WebDriverIE(this.filterAttributes, this.crawlWaitReload,
				        this.crawlWaitEvent);

			case chrome:
				return new WebDriverChrome(this.filterAttributes, this.crawlWaitReload,
				        this.crawlWaitEvent);

			default:
				return new WebDriverFirefox(this.filterAttributes, this.crawlWaitReload,
				        this.crawlWaitEvent);
		}
	}

	/**
	 * Close all browser windows.
	 */
	public synchronized void close() {
		Queue<EmbeddedBrowser> deleteList = new LinkedList<EmbeddedBrowser>();
		if (useBooting()) {
			booter.shutdown();
		}
		for (EmbeddedBrowser b : available) {
			try {
				b.close();
			} catch (Exception e) {
				LOGGER.error("Failed to close free Browser " + b, e);
			} finally {
				deleteList.add(b);
			}
		}
		available.removeAll(deleteList);
		deleteList = new LinkedList<EmbeddedBrowser>();
		for (EmbeddedBrowser b : taken) {
			try {
				b.close();
			} catch (Exception e) {
				LOGGER.error("Failed to close taken Browser " + b, e);
			} finally {
				deleteList.add(b);
			}
		}
		taken.removeAll(deleteList);

		assert (available.isEmpty());
		assert (taken.isEmpty());

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
						LOGGER.fatal("I could (might) not rescue a browser creation!", e);
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
		private final BrowserFactory factory;

		public BrowserBooter(BrowserFactory factory) {
			assert (factory != null);
			this.factory = factory;
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
			for (; i < factory.getNumberOfBrowsers(); i++) {
				// Create a new Thread
				new Thread(new Runnable() {
					private int bootRetries = 0;

					@Override
					public void run() {
						try {
							factory.available.add(factory.createBrowser());
							createdBrowserCount.incrementAndGet();
						} catch (WebDriverException e) {
							LOGGER.error("Creation of Browser faild!", e);
							if (factory.getNumberBrowserCreateRetries() > 0
							        && bootRetries < factory.getNumberBrowserCreateRetries()) {
								bootRetries++;

								// Do we need to sleep after a crash?
								if (factory.getSleepTimeOnBrowserCreationFailure() > 0) {
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
								LOGGER.error("Could not rescure browser creation!", e);
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
			        + factory.getNumberOfBrowsers());
			while (!allBrowsersLoaded()) {
				try {
					Thread.sleep(factory.shutdownTimeout);
				} catch (InterruptedException e) {
					LOGGER.error("Closing of the browsers faild due to an Interrupt", e);
				}
			}
		}

		/**
		 * Start this Thread, the Thread will only be started when it is called the first time.
		 */
		@Override
		public synchronized void start() {
			if (!finished && started.compareAndSet(false, true)) {
				super.start();
			}
			assert (started.get());
		}

		/**
		 * @return true is all requested browsers are loaded or at least there was an attempt for!.
		 */
		private boolean allBrowsersLoaded() {
			return (failedCreatedBrowserCount.get() + createdBrowserCount.get()) >= factory
			        .getNumberOfBrowsers();
		}
	}
}
