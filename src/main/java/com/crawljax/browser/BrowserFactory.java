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
 * @version $Id$
 */
public final class BrowserFactory {

	private static final Queue<EmbeddedBrowser> freeBrowerList =
	        new LinkedList<EmbeddedBrowser>();
	private static final ArrayList<EmbeddedBrowser> takenBrowerList =
	        new ArrayList<EmbeddedBrowser>();

	private static Logger LOGGER = Logger.getLogger(BrowserFactory.class);

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
	public static final String getBrowserTypeString() {
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
	 * Internal used to requestBrowser
	 * 
	 * @see {@link #requestBrowser()}
	 * @return the new browser
	 */
	private static final EmbeddedBrowser makeABrowser() {
		if (freeBrowerList.size() == 0 && takenBrowerList.size() == 0
		        && PropertyHelper.getCrawljaxConfiguration() != null) {
			// This should be the first browser? use the one from the config
			// More are 'cloned' later....
			return PropertyHelper.getCrawljaxConfiguration().getBrowser();
		}

		EmbeddedBrowser browser = null;
		try {
			browser = findBrowserClass().newInstance();
		} catch (InstantiationException e) {
			LOGGER.error("Cannot create a new Browser!", e);
		} catch (IllegalAccessException e) {
			LOGGER.error("Cannot create a new Browser!", e);
		}
		return browser;
	}

	/**
	 * Close all browser windows
	 */
	public static synchronized void close() {
		Queue<EmbeddedBrowser> deleteList = new LinkedList<EmbeddedBrowser>();
		for (EmbeddedBrowser b : freeBrowerList) {
			try {
				b.close();
				deleteList.add(b);
			} catch (Exception e) {
				LOGGER.error("Faild to close free Browser " + b, e);
			}
		}
		freeBrowerList.removeAll(deleteList);
		deleteList = new LinkedList<EmbeddedBrowser>();
		for (EmbeddedBrowser b : takenBrowerList) {
			try {
				b.close();
				deleteList.add(b);
			} catch (Exception e) {
				LOGGER.error("Faild to close taken Browser " + b, e);
			}
		}
		takenBrowerList.removeAll(deleteList);
	}

	/**
	 * Return the browser, release the current browser for further operations.
	 * 
	 * @param browser
	 *            the browser which is not needed anymore
	 */
	public static synchronized void freeBrowser(EmbeddedBrowser browser) {
		takenBrowerList.remove(browser);
		freeBrowerList.add(browser);
	}

	/**
	 * Place a request for a browser
	 * 
	 * @return a new Browser instance which is currently free
	 */
	public static synchronized EmbeddedBrowser requestBrowser() {
		EmbeddedBrowser browser;
		if (freeBrowerList.size() > 0) {
			// Retrieve a free browser from the list
			browser = freeBrowerList.poll();
			if (browser == null) {
				LOGGER.error("Faild to fulfil a request for a browser");
			}
		} else {
			// There is no browser free...
			// Create a new Browser
			browser = makeABrowser();
		}
		takenBrowerList.add(browser);

		return browser;
	}

	/**
	 * Are all takenBrowsers free (again)?
	 * 
	 * @see ArrayList#isEmpty()
	 * @return true if the taken list of Browsers is empty
	 */
	public static synchronized boolean isFinished() {
		return takenBrowerList.isEmpty();
	}

}
