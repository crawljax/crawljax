/**
 * Created Jan 18, 2008
 */
package com.crawljax.browser;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.crawljax.util.PropertyHelper;

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
	private static final BlockingQueue<EmbeddedBrowser> AVAILABLE =
	        new ArrayBlockingQueue<EmbeddedBrowser>(PropertyHelper.getCrawNumberOfThreadsValue(),
	                true);

	/**
	 * ConcurrentLinkedQueue used to store the taken browsers.
	 */
	private static final ConcurrentLinkedQueue<EmbeddedBrowser> TAKEN =
	        new ConcurrentLinkedQueue<EmbeddedBrowser>();

	/**
	 * Boot class used to handle and monitor the boot process.
	 */
	private static final BrowserBooter BOOTER = new BrowserBooter();

	/**
	 * The amount of time used to wait between every check for the close operation.
	 */
	private static final int SHUTDOWNTIMEOUT = 100;

	/**
	 * hidden constructor.
	 */
	private BrowserFactory() {
	}

	/**
	 * What is the type of browser that will be used?
	 * 
	 * @return the name of the type that will be used
	 */
	public static String getBrowserTypeString() {
		return findBrowserClass().getName();
	}

	/**
	 * Internal used to retrieve the class which should be used to init a new browser.
	 * 
	 * @return the Class that should be used.
	 */
	private static Class<? extends EmbeddedBrowser> findBrowserClass() {
		if (PropertyHelper.getCrawljaxConfiguration() != null) {
			return PropertyHelper.getCrawljaxConfiguration().getBrowser().getClass();
		}
		String browser = PropertyHelper.getBrowserValue();
		if ("webdriver.ie".equals(browser)) {
			return WebDriverIE.class;
		}
		return WebDriverFirefox.class;
	}

	/**
	 * Internal used to requestBrowser.
	 * 
	 * @see {@link #requestBrowser()}
	 * @return the new browser
	 */
	private static EmbeddedBrowser createBrowser() {
		if (AVAILABLE.isEmpty() && TAKEN.isEmpty()
		        && PropertyHelper.getCrawljaxConfiguration() != null) {
			// This should be the first browser? use the one from the config
			// More are 'cloned' later....
			return PropertyHelper.getCrawljaxConfiguration().getBrowser();
		}

		EmbeddedBrowser newBrowser = null;
		EmbeddedBrowser currentBrowser = null;
		if (PropertyHelper.getCrawljaxConfiguration() != null) {
			currentBrowser = PropertyHelper.getCrawljaxConfiguration().getBrowser();
		}
		if (currentBrowser != null) {
			// Clone the Browser
			newBrowser = currentBrowser.clone();
		} else {
			// There is no browser specified so try to find the class to use and instance it by
			// reflection
			try {
				newBrowser = findBrowserClass().newInstance();
			} catch (InstantiationException e) {
				LOGGER.error("Cannot create a new Browser!", e);
			} catch (IllegalAccessException e) {
				LOGGER.error("Cannot create a new Browser!", e);
			}
		}
		return newBrowser;
	}

	/**
	 * Close all browser windows.
	 */
	public static synchronized void close() {
		Queue<EmbeddedBrowser> deleteList = new LinkedList<EmbeddedBrowser>();
		if (useBooting()) {
			BOOTER.shutdown();
		}
		for (EmbeddedBrowser b : AVAILABLE) {
			try {
				b.close();
				deleteList.add(b);
			} catch (Exception e) {
				LOGGER.error("Failed to close free Browser " + b, e);
			}
		}
		AVAILABLE.removeAll(deleteList);
		deleteList = new LinkedList<EmbeddedBrowser>();
		for (EmbeddedBrowser b : TAKEN) {
			try {
				b.close();
				deleteList.add(b);
			} catch (Exception e) {
				LOGGER.error("Failed to close taken Browser " + b, e);
			}
		}
		TAKEN.removeAll(deleteList);
	}

	/**
	 * Return the browser, release the current browser for further operations. This method is
	 * Synchronised to prevent problems in combination with isFinished.
	 * 
	 * @param browser
	 *            the browser which is not needed anymore
	 */
	public static synchronized void freeBrowser(EmbeddedBrowser browser) {
		TAKEN.remove(browser);
		AVAILABLE.add(browser);
	}

	/**
	 * Place a request for a browser.
	 * 
	 * @return a new Browser instance which is currently free
	 * @throws InterruptedException
	 *             the InterruptedException is thrown when the AVAILABE list is interrupted.
	 */
	public static EmbeddedBrowser requestBrowser() throws InterruptedException {
		EmbeddedBrowser browser = null;
		if (useBooting()) {
			BOOTER.start();
			browser = waitForBrowser();
		} else {
			if (AVAILABLE.size() > 0) {
				browser = AVAILABLE.take();
			} else {
				browser = createBrowser();
			}
			TAKEN.add(browser);
		}
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
	 *             when AVAILABLE.take is Interrupted
	 */
	private static EmbeddedBrowser waitForBrowser() throws InterruptedException {
		EmbeddedBrowser b = AVAILABLE.take();
		TAKEN.add(b);
		return b;
	}

	/**
	 * Are all takenBrowsers free (again)? this method is Synchronised to prevent problems in
	 * combination with freeBrowser.
	 * 
	 * @return true if the taken list of Browsers is empty
	 */
	public static synchronized boolean isFinished() {
		return TAKEN.isEmpty();
	}

	/**
	 * This private class is used to boot all the browsers.
	 * 
	 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
	 */
	private static class BrowserBooter extends Thread {
		private boolean finished = false;
		private final AtomicInteger createdBrowserCount = new AtomicInteger(0);

		@Override
		public void run() {
			int i = 0;
			if (AVAILABLE.isEmpty() && TAKEN.isEmpty()
			        && PropertyHelper.getCrawljaxConfiguration() != null) {
				AVAILABLE.add(PropertyHelper.getCrawljaxConfiguration().getBrowser());
				i = 1;
			}

			createdBrowserCount.set(i);

			// Loop to the requested number of browsers
			for (; i < PropertyHelper.getCrawNumberOfThreadsValue(); i++) {
				// Create a new Thread
				new Thread(new Runnable() {

					@Override
					public void run() {
						AVAILABLE.add(createBrowser());
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
			if (allBrowsersLoaded()) {
				return;
			}
			LOGGER.warn("Waiting for all browsers to be started fully"
			        + " before starting to close them");
			while (!BOOTER.allBrowsersLoaded()) {
				try {
					Thread.sleep(SHUTDOWNTIMEOUT);
				} catch (InterruptedException e) {
					LOGGER.error("Closing of the browsers faild du to an Interrupt", e);
				}
			}
		}

		/**
		 * Start this Thread, the Thread will only be started when it is called the first time.
		 */
		@Override
		public synchronized void start() {
			if (!finished && !isAlive()) {
				super.start();
			}
		}

		/**
		 * @return true is all requested browsers are loaded.
		 */
		private boolean allBrowsersLoaded() {
			return createdBrowserCount.get() >= PropertyHelper.getCrawNumberOfThreadsValue();
		}
	}
}
