package com.crawljax.browser;

import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.Timeout;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.test.BrowserTest;

/**
 * This test, test the (public) operations from the Browserpool.
 */
@Category(BrowserTest.class)
public class BrowserPoolTest {
	private BrowserPool pool;

	@ClassRule
	public static final Timeout TIME_OUT = new Timeout((int) TimeUnit.SECONDS.toMillis(100));

	@Before
	public void setup() {
		CrawljaxConfiguration config =
		        CrawljaxConfiguration.builderFor("http://localhost").build();
		pool = new BrowserPool(config);
	}

	/**
	 * Request don't release and close the pool.
	 * 
	 * @throws InterruptedException
	 *             thrown from requestBrowser
	 */
	@Test
	public void testRequestClose() throws InterruptedException {

		pool.requestBrowser();
		Thread closeThread = pool.close();
		closeThread.join();
	}

	/**
	 * Request a browser, release it and close the pool.
	 * 
	 * @throws InterruptedException
	 *             thrown from requestBrowser
	 */
	@Test
	public void testRequestReleaseClose() throws InterruptedException {
		EmbeddedBrowser b = pool.requestBrowser();
		pool.freeBrowser(b);
		Thread closeThread = pool.close();
		closeThread.join();
	}

	/**
	 * Test a call to two times the close operation, after a browser request.
	 * 
	 * @throws InterruptedException
	 *             thrown from requestBrowser
	 */
	public void testDoubleClose() throws InterruptedException {
		pool.requestBrowser();
		Thread closeThread = pool.close();
		closeThread.join();
		closeThread = pool.close();
		closeThread.join();

	}

	/**
	 * Test a call to close only.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testCloseOnly() throws InterruptedException {
		Thread closeThread = pool.close();
		closeThread.join();
	}

	/**
	 * Test a call to close only twice.
	 * 
	 * @throws InterruptedException
	 *             TODO Stefan turn on again if it is stable
	 */
	@Test
	public void testCloseOnlyTwoTimes() throws InterruptedException {
		// TODO Stefan, what about two times without join?
		Thread closeThread = pool.close();
		closeThread.join();
		closeThread = pool.close();
		closeThread.join();
	}

	/**
	 * Test opening 4 browsers, 3 requested, 1 returned. close should be done within TIMEOUT.
	 * 
	 * @throws ConfigurationException
	 *             when config is not correct
	 * @throws InterruptedException
	 *             when the request for a browser is interupped TODO Stefan turn on again if it is
	 *             stable
	 */
	@Test
	public void testMultipleBrowsers() throws ConfigurationException, InterruptedException {
		BrowserPool pool = new BrowserPool(configForNumberOfBrowsers(3, false));

		pool.requestBrowser();
		pool.requestBrowser();
		EmbeddedBrowser b1 = pool.requestBrowser();
		pool.freeBrowser(b1);

		pool.shutdown();
	}

	private CrawljaxConfiguration configForNumberOfBrowsers(int number, boolean boot) {
		return CrawljaxConfiguration.builderFor("http://localhost")
		        .setBrowserConfig(new BrowserConfiguration(BrowserType.firefox, number, boot))
		        .build();
	}

	/**
	 * Test opening 4 browsers, 3 requested, 1 returned. close should be done within TIMEOUT. This
	 * time using the fast boot option
	 * 
	 * @throws ConfigurationException
	 *             when config is not correct
	 * @throws InterruptedException
	 *             when the request for a browser is interupped TODO Stefan turn on again if it is
	 *             stable
	 */
	@Test
	public void testMultipleBrowsersFastBoot() throws ConfigurationException,
	        InterruptedException {
		BrowserPool pool = new BrowserPool(configForNumberOfBrowsers(4, true));

		pool.requestBrowser();
		pool.requestBrowser();
		EmbeddedBrowser b1 = pool.requestBrowser();
		pool.freeBrowser(b1);

		pool.shutdown();
	}

}