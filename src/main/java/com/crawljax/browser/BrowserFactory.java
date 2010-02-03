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

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.ProxyConfiguration;

/**
 * The factory class returns an instance of the desired browser as specified in the properties file.
 * TODO Stefan; add getCrawlNumberOfBrowsers to the config and implement.
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

	private final int numberOfThreads;

	private long crawlWaitReload;

	private List<String> filterAttributes;

	private long crawlWaitEvent;

	/**
	 * @return the number of threads.
	 */
	protected int getNumberOfThreads() {
		return numberOfThreads;
	}

	/**
	 * @return the browser type.
	 */
	public BrowserType getBrowserType() {
		return browserType;
	}

	/**
	 * hidden constructor.
	 * 
	 * @param type
	 *            the browser type.
	 * @param numberOfThreads
	 *            number of threads to use.
	 * @param proxyConfig
	 *            the proxy configuration (can be null).
	 * @param filterAttributes
	 *            the attributes to be filtered from DOM.
	 * @param crawlWaitReload
	 *            the period to wait after a reload.
	 * @param crawlWaitEvent
	 *            the period to wait after an event is fired.
	 */
	public BrowserFactory(BrowserType type, int numberOfThreads, ProxyConfiguration proxyConfig,
	        List<String> filterAttributes, long crawlWaitReload, long crawlWaitEvent) {

		this.available = new ArrayBlockingQueue<EmbeddedBrowser>(numberOfThreads, true);
		this.browserType = type;
		this.proxyConfiguration = proxyConfig;
		this.numberOfThreads = numberOfThreads;
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
			// There is no browser specified so try to find the class to use and instance it by
			// reflection

			newBrowser = getBrowserInstance();

		}
		assert (newBrowser != null);
		return newBrowser;

	}

	private EmbeddedBrowser getBrowserInstance() {

		switch (browserType) {
			case firefox:
				if (proxyConfiguration != null) {
					return new WebDriverFirefox(proxyConfiguration, this.filterAttributes,
					        this.crawlWaitReload, this.crawlWaitEvent);
				}
				return new WebDriverFirefox(this.filterAttributes, this.crawlWaitReload,
				        this.crawlWaitEvent);

			case ie:
				return new WebDriverIE(this.filterAttributes, this.crawlWaitReload,
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
		// assert (!factory.taken.contains(browser));
		// assert (factory.available.contains(browser));
	}

	/**
	 * Place a request for a browser.
	 * 
	 * @return a new Browser instance which is currently free
	 * @throws InterruptedException
	 *             the InterruptedException is thrown when the AVAILABE list is interrupted.
	 */
	public EmbeddedBrowser requestBrowser() throws InterruptedException {

		EmbeddedBrowser browser;
		if (useBooting()) {
			booter.start();
			browser = waitForBrowser();
		} else {
			if (available.size() > 0) {
				browser = available.take();
			} else {
				browser = createBrowser();
			}
			taken.add(browser);
		}
		assert (browser != null);
		assert (taken.contains(browser));
		return browser;
	}

	private static boolean useBooting() {
		// TODO Stefan Config...
		return true;
	}

	/**
	 * This call blocks until a browser comes available.
	 * 
	 * @return the browser currently reserved.
	 * @throws InterruptedException
	 *             when available.take is Interrupted
	 */
	private synchronized EmbeddedBrowser waitForBrowser() throws InterruptedException {
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
		private final BrowserFactory factory;

		public BrowserBooter(BrowserFactory factory) {
			assert (factory != null);
			this.factory = factory;
			started = new AtomicBoolean(false);
			createdBrowserCount = new AtomicInteger(0);
		}

		@Override
		public void run() {
			int i = 0;

			createdBrowserCount.set(i);

			assert (createdBrowserCount.get() <= 1);

			// Loop to the requested number of browsers
			for (; i < factory.getNumberOfThreads(); i++) {
				// Create a new Thread
				new Thread(new Runnable() {

					@Override
					public void run() {
						factory.available.add(factory.createBrowser());
						createdBrowserCount.incrementAndGet();
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
			        + factory.getNumberOfThreads());
			while (!allBrowsersLoaded()) {
				try {
					Thread.sleep(factory.shutdownTimeout);
				} catch (InterruptedException e) {
					LOGGER.error("Closing of the browsers faild due to an Interrupt", e);
				}
			}
			assert (allBrowsersLoaded());
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
		 * @return true is all requested browsers are loaded.
		 */
		private boolean allBrowsersLoaded() {
			return createdBrowserCount.get() >= factory.getNumberOfThreads();
		}
	}
}
