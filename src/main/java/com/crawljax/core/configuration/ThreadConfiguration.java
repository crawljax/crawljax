package com.crawljax.core.configuration;

import org.openqa.selenium.firefox.FirefoxDriver;

/**
 * This class denotes all the configuration variable that can be set with regard to the number of
 * Threads active.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id$
 */
public class ThreadConfiguration {

	private final int baseFreePortNumber = 32768;
	private final int limitFreePortNumber = 65535;

	/**
	 * Default SleepTimeOnBrowserCreationFailure == 10 seconds == 10000 Millisecond.
	 */
	private final int defaultSleepTimeOnBrowserCreationFailure = 10000;

	/**
	 * The number of threads to run at the same time inside Crawljax. Default is 1. when this is not
	 * set and the number of browsers is set the number of threads equals the number of browsers.
	 */
	private int numberThreads = 1;

	/**
	 * Internal only flag variable.
	 */
	private boolean numberThreadsSet = false;

	/**
	 * The maximum number of opened browsers to use. Default if this is not set it is set to the
	 * number of threads {@link #numberThreads}
	 */
	private final int numberBrowsers;

	/**
	 * Internal only flag variable.
	 */
	private boolean numberBrowsersSet = false;

	/**
	 * is the Booting (pre-loading) of browsers in use? Default to true;
	 */
	private boolean browserBooting = false;

	/**
	 * The total number of retries when a browser can not be created.
	 */
	private final int numberBrowserCreateRetries = 0;

	/**
	 * The number of milliseconds to sleep when a browser can not be created.
	 */
	private final int sleepTimeOnBrowserCreationFailure =
	        defaultSleepTimeOnBrowserCreationFailure;

	/**
	 * This field indicates if the fastBooting algorithm must be used.
	 */
	private final boolean useFastBooting = false;

	/**
	 * Use a random port number.
	 */
	private final boolean useRandomPortNumberCreation = true;

	/**
	 * The last returned port number.
	 */
	private int lastPort = FirefoxDriver.DEFAULT_PORT;

	/**
	 * Initialise everything directly.
	 * 
	 * @param browsers
	 *            the number of browsers to use
	 * @param threads
	 *            the number of threads to use
	 * @param useBooting
	 *            is the booting in use?
	 */
	private ThreadConfiguration(int browsers, int threads, boolean useBooting) {
		numberThreadsSet = true;
		numberBrowsersSet = true;
		numberBrowsers = browsers;
		numberThreads = threads;
		browserBooting = useBooting;
	}

	/**
	 * Default 'init' constructor.
	 */
	public ThreadConfiguration() {
		this(1, 1, false);
		/**
		 * Default everything.
		 */
		numberThreadsSet = false;
		numberBrowsersSet = false;
	}

	/**
	 * @return the numberThreads
	 */
	public final int getNumberThreads() {
		if (!numberThreadsSet && numberBrowsersSet) {
			return numberBrowsers;
		}
		return numberThreads;
	}

	/**
	 * @return the numberBrowsers
	 */
	public final int getNumberBrowsers() {
		if (!numberBrowsersSet) {
			return getNumberThreads();
		}
		return numberBrowsers;
	}

	/**
	 * @return the browserBooting
	 */
	public final boolean isBrowserBooting() {
		return browserBooting;
	}

	/**
	 * @return the total number of retries when a browser can not be created.
	 */
	public final int getNumberBrowserCreateRetries() {
		return numberBrowserCreateRetries;
	}

	/**
	 * @return the number of milliseconds to sleep when a browser can not be created.
	 */
	public final int getSleepTimeOnBrowserCreationFailure() {
		return sleepTimeOnBrowserCreationFailure;
	}

	/**
	 * Is the fast booting algorithm in use?
	 * 
	 * @return true if the fast booting is in use
	 */
	public boolean getUseFastBooting() {
		return this.useFastBooting;
	}

	/**
	 * Determine a port number depending on the configuration value.
	 * 
	 * @return the port number to use;
	 */
	public int getPortNumber() {
		if (getUseFastBooting()) {
			if (useRandomPortNumberCreation) {
				return baseFreePortNumber
				        + (int) (Math.random() * (limitFreePortNumber - baseFreePortNumber));
			} else {
				return this.lastPort++;
			}
		} else {
			return this.lastPort;
		}
	}

	// TODO Stefan insert all Set operations from r312
}
