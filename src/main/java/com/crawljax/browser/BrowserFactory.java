/**
 * Created Jan 18, 2008
 */
package com.crawljax.browser;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
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
	private final BlockingQueue<EmbeddedBrowser> available =
	        new ArrayBlockingQueue<EmbeddedBrowser>(PropertyHelper.getCrawNumberOfThreadsValue(),
	                true);

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

	/**
	 * Private instance running in HEAP-space.
	 */
	private static BrowserFactory instance;

	/**
	 * hidden constructor.
	 */
	private BrowserFactory() {
		booter = new BrowserBooter(this);
		assert (!booter.isAlive());
	}

	/**
	 * Return a new instance of the current BrowserFactory in use. The instance must be returned by
	 * Finally calling {@link #close()}.
	 * 
	 * @return the current running instance of the BrowserFactory.
	 */
	private static synchronized BrowserFactory instance() {
		if (instance == null) {
			instance = new BrowserFactory();
		}
		return instance;
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
	private EmbeddedBrowser createBrowser() {
		if (available.isEmpty() && taken.isEmpty()
		        && PropertyHelper.getCrawljaxConfiguration() != null) {
			// This should be the first browser? use the one from the config
			// More are 'cloned' later....
			EmbeddedBrowser browser = PropertyHelper.getCrawljaxConfiguration().getBrowser();
			assert (browser != null);
			return browser;
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
		assert (newBrowser != null);
		return newBrowser;
	}

	/**
	 * Close all browser windows.
	 */
	public static synchronized void close() {
		BrowserFactory factory = instance();
		Queue<EmbeddedBrowser> deleteList = new LinkedList<EmbeddedBrowser>();
		if (useBooting()) {
			factory.booter.shutdown();
		}
		for (EmbeddedBrowser b : factory.available) {
			try {
				b.close();
			} catch (Exception e) {
				LOGGER.error("Failed to close free Browser " + b, e);
			} finally {
				deleteList.add(b);
			}
		}
		factory.available.removeAll(deleteList);
		deleteList = new LinkedList<EmbeddedBrowser>();
		for (EmbeddedBrowser b : factory.taken) {
			try {
				b.close();
			} catch (Exception e) {
				LOGGER.error("Failed to close taken Browser " + b, e);
			} finally {
				deleteList.add(b);
			}
		}
		factory.taken.removeAll(deleteList);

		assert (factory.available.isEmpty());
		assert (factory.taken.isEmpty());

		// Delete the factory instance.
		instance = null;
		assert (instance == null);
	}

	/**
	 * Return the browser, release the current browser for further operations. This method is
	 * Synchronised to prevent problems in combination with isFinished.
	 * 
	 * @param browser
	 *            the browser which is not needed anymore
	 */
	public static synchronized void freeBrowser(EmbeddedBrowser browser) {
		BrowserFactory factory = instance();
		assert (browser != null);
		factory.taken.remove(browser);
		factory.available.add(browser);
		assert (!factory.taken.contains(browser));
		assert (factory.available.contains(browser));
	}

	/**
	 * Place a request for a browser.
	 * 
	 * @return a new Browser instance which is currently free
	 * @throws InterruptedException
	 *             the InterruptedException is thrown when the AVAILABE list is interrupted.
	 */
	public static EmbeddedBrowser requestBrowser() throws InterruptedException {
		BrowserFactory factory = instance();
		EmbeddedBrowser browser;
		if (useBooting()) {
			factory.booter.start();
			browser = factory.waitForBrowser();
		} else {
			if (factory.available.size() > 0) {
				browser = factory.available.take();
			} else {
				browser = factory.createBrowser();
			}
			factory.taken.add(browser);
		}
		assert (browser != null);
		assert (factory.taken.contains(browser));
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
	public static synchronized boolean isFinished() {
		BrowserFactory instance = instance();
		return instance.taken.isEmpty();
	}

	/**
	 * This private class is used to boot all the browsers.
	 * 
	 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
	 */
	private static class BrowserBooter extends Thread {
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
			if (factory.available.isEmpty() && factory.taken.isEmpty()
			        && PropertyHelper.getCrawljaxConfiguration() != null) {
				factory.available.add(PropertyHelper.getCrawljaxConfiguration().getBrowser());
				i = 1;
			}

			createdBrowserCount.set(i);

			assert (createdBrowserCount.get() <= 1);

			// Loop to the requested number of browsers
			for (; i < PropertyHelper.getCrawNumberOfThreadsValue(); i++) {
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
			        + PropertyHelper.getCrawNumberOfThreadsValue());
			while (!allBrowsersLoaded()) {
				try {
					Thread.sleep(factory.shutdownTimeout);
				} catch (InterruptedException e) {
					LOGGER.error("Closing of the browsers faild du to an Interrupt", e);
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
			assert (PropertyHelper.getCrawNumberOfThreadsValue() > 0);

			return createdBrowserCount.get() >= PropertyHelper.getCrawNumberOfThreadsValue();
		}
	}
}
