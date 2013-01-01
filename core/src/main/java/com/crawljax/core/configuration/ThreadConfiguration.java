package com.crawljax.core.configuration;

/**
 * This class denotes all the configuration variable that can be set with regard to the number of
 * Threads active.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id$
 */
public class ThreadConfiguration {

	/**
	 * Default SleepTimeOnBrowserCreationFailure == 10 seconds == 10000 Millisecond.
	 */
	private static final int DEFAULT_SLEEP_TIME_ON_BROWSER_CREATION_FAILURE = 10000;

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
	private int numberBrowsers;

	/**
	 * Internal only flag variable.
	 */
	private boolean numberBrowsersSet = false;

	/**
	 * is the Booting (pre-loading) of browsers in use? Default to true;
	 */
	private boolean browserBooting = true;

	/**
	 * The total number of retries when a browser can not be created.
	 */
	private int numberBrowserCreateRetries = 2;

	/**
	 * The number of milliseconds to sleep when a browser can not be created.
	 */
	private int sleepTimeOnBrowserCreationFailure =
	        DEFAULT_SLEEP_TIME_ON_BROWSER_CREATION_FAILURE;

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
	public ThreadConfiguration(int browsers, int threads, boolean useBooting) {
		numberThreadsSet = true;
		numberBrowsersSet = true;
		numberBrowsers = browsers;
		numberThreads = threads;
		browserBooting = useBooting;
	}

	/**
	 * Specify only the number of browsers. This constructor makes the number of threads equal to
	 * the number of browsers.
	 * 
	 * @param browsers
	 *            the number of browsers to use.
	 */
	public ThreadConfiguration(int browsers) {
		this(browsers, browsers, true);
		numberThreadsSet = false;
	}

	/**
	 * Default 'init' constructor.
	 */
	public ThreadConfiguration() {
		this(1, 1, true);
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
	 * @param numberThreads
	 *            the numberThreads to set
	 */
	public final void setNumberThreads(int numberThreads) {
		numberThreadsSet = true;
		this.numberThreads = numberThreads;
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
	 * @param numberBrowsers
	 *            the numberBrowsers to set
	 */
	public final void setNumberBrowsers(int numberBrowsers) {
		numberBrowsersSet = true;
		this.numberBrowsers = numberBrowsers;
	}

	/**
	 * @return the browserBooting
	 */
	public final boolean isBrowserBooting() {
		return browserBooting;
	}

	/**
	 * @param browserBooting
	 *            the browserBooting to set
	 */
	public final void setBrowserBooting(boolean browserBooting) {
		this.browserBooting = browserBooting;
	}

	/**
	 * @return the total number of retries when a browser can not be created.
	 */
	public final int getNumberBrowserCreateRetries() {
		return numberBrowserCreateRetries;
	}

	/**
	 * The total number of retries when a browser can not be created.
	 * 
	 * @param numberBrowserCreateRetries
	 *            the numberBrowserCreateRetries to set
	 */
	public final void setNumberBrowserCreateRetries(int numberBrowserCreateRetries) {
		this.numberBrowserCreateRetries = numberBrowserCreateRetries;
	}

	/**
	 * @return the number of milliseconds to sleep when a browser can not be created.
	 */
	public final int getSleepTimeOnBrowserCreationFailure() {
		return sleepTimeOnBrowserCreationFailure;
	}

	/**
	 * The number of milliseconds to sleep when a browser can not be created.
	 * 
	 * @param sleepTimeOnBrowserCreationFailure
	 *            the sleepTimeOnBrowserCreationFailure to set
	 */
	public final void setSleepTimeOnBrowserCreationFailure(int sleepTimeOnBrowserCreationFailure) {
		this.sleepTimeOnBrowserCreationFailure = sleepTimeOnBrowserCreationFailure;
	}

}
