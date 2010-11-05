package com.crawljax.core.configuration;

/**
 * This class is the reader class for the threadConfiguration.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id$
 */
public class ThreadConfigurationReader {
	private final ThreadConfiguration threadConfiguration;

	/**
	 * A reader class for the ThreadConfiguration.
	 * 
	 * @param threadConfiguration
	 *            the actual config object.
	 */
	public ThreadConfigurationReader(ThreadConfiguration threadConfiguration) {
		this.threadConfiguration = threadConfiguration;
	}

	/**
	 * @return the number of Browsers to use
	 * @see com.crawljax.core.configuration.ThreadConfiguration#getNumberBrowsers()
	 */
	public final int getNumberBrowsers() {
		return threadConfiguration.getNumberBrowsers();
	}

	/**
	 * @return the number of threads to use
	 * @see com.crawljax.core.configuration.ThreadConfiguration#getNumberThreads()
	 */
	public final int getNumberThreads() {
		return threadConfiguration.getNumberThreads();
	}

	/**
	 * @return is the Browser pre-booting in use?
	 * @see com.crawljax.core.configuration.ThreadConfiguration#isBrowserBooting()
	 */
	public final boolean isBrowserBooting() {
		return threadConfiguration.isBrowserBooting();
	}

	/**
	 * @return the total number of retries when a browser can not be created.
	 * @see com.crawljax.core.configuration.ThreadConfiguration#getNumberBrowserCreateRetries()
	 */
	public final int getNumberBrowserCreateRetries() {
		return threadConfiguration.getNumberBrowserCreateRetries();
	}

	/**
	 * @return the number of milliseconds to sleep when a browser can not be created.
	 * @see com.crawljax.core.configuration.ThreadConfiguration#
	 *      getSleepTimeOnBrowserCreationFailure()
	 */
	public final int getSleepTimeOnBrowserCreationFailure() {
		return threadConfiguration.getSleepTimeOnBrowserCreationFailure();
	}

}
