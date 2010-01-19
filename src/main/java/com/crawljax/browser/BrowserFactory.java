/**
 * Created Jan 18, 2008
 */
package com.crawljax.browser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;

import com.crawljax.util.PropertyHelper;

/**
 * The factory class returns an instance of the desired browser as specified in the properties file.
 * 
 * @author mesbah
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id$
 */
public final class BrowserFactory {

	private static final Queue<EmbeddedBrowser> FREE_BROWSERS = new LinkedList<EmbeddedBrowser>();
	private static final ArrayList<EmbeddedBrowser> TAKEN_BROWSERS =
	        new ArrayList<EmbeddedBrowser>();

	private static final Logger LOGGER = Logger.getLogger(BrowserFactory.class);

	/**
	 * The booting flag indicates that the BrowserFactory is still in process of booting. If the
	 * value is changed from true to false and the booting is still in progress no new browsers will
	 * be made.
	 */
	private static boolean booting = false;

	/**
	 * This field is set to true when the creation of browsers is finished.
	 */
	private static boolean doneBooting = false;

	private static final int TEN = 10;

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
	private static EmbeddedBrowser makeABrowser() {
		if (FREE_BROWSERS.size() == 0 && TAKEN_BROWSERS.size() == 0
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
		if (booting) {
			booting = false;
			while (!doneBooting) {
				try {
					Thread.sleep(TEN);
				} catch (InterruptedException e) {
					LOGGER.error("Closing of the browsers faild du to an Interrupt", e);
				}
			}
		}
		for (EmbeddedBrowser b : FREE_BROWSERS) {
			try {
				b.close();
				deleteList.add(b);
			} catch (Exception e) {
				LOGGER.error("Failed to close free Browser " + b, e);
			}
		}
		FREE_BROWSERS.removeAll(deleteList);
		deleteList = new LinkedList<EmbeddedBrowser>();
		for (EmbeddedBrowser b : TAKEN_BROWSERS) {
			try {
				b.close();
				deleteList.add(b);
			} catch (Exception e) {
				LOGGER.error("Failed to close taken Browser " + b, e);
			}
		}
		TAKEN_BROWSERS.removeAll(deleteList);
	}

	/**
	 * Return the browser, release the current browser for further operations.
	 * 
	 * @param browser
	 *            the browser which is not needed anymore
	 */
	public static synchronized void freeBrowser(EmbeddedBrowser browser) {
		TAKEN_BROWSERS.remove(browser);
		FREE_BROWSERS.add(browser);
	}

	/**
	 * Place a request for a browser.
	 * 
	 * @return a new Browser instance which is currently free
	 */
	public static synchronized EmbeddedBrowser requestBrowser() {
		EmbeddedBrowser browser;
		if (FREE_BROWSERS.size() > 0) {
			// Retrieve a free browser from the list
			synchronized (FREE_BROWSERS) {
				browser = FREE_BROWSERS.poll();
			}
			if (browser == null) {
				LOGGER.error("Failed to fulfill a request for a browser");
			}
		} else {
			// There is no browser free...
			// Create a new Browser
			browser = makeABrowser();
		}
		TAKEN_BROWSERS.add(browser);

		if (!booting && !doneBooting) {
			booting = true;
			new Thread(new Runnable() {

				@Override
				public void run() {
					while (booting
					        && (FREE_BROWSERS.size() + TAKEN_BROWSERS.size()) != PropertyHelper
					                .getCrawNumberOfThreadsValue()) {

						EmbeddedBrowser b = makeABrowser();
						synchronized (FREE_BROWSERS) {
							FREE_BROWSERS.add(b);
						}
					}
					booting = false;
					doneBooting = true;
				}
			}).start();
		}
		return browser;
	}

	/**
	 * Are all takenBrowsers free (again)?
	 * 
	 * @see ArrayList#isEmpty()
	 * @return true if the taken list of Browsers is empty
	 */
	public static synchronized boolean isFinished() {
		return TAKEN_BROWSERS.isEmpty();
	}

}
