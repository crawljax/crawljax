package com.crawljax.browser;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfigurationReader;
import com.crawljax.core.configuration.ThreadConfiguration;
import com.crawljax.test.BrowserTest;

/**
 * This test, test the (public) operations from the Browserpool.
 */
@Category(BrowserTest.class)
public class BrowserPoolTest {
	private static final int TIMEOUT = 100000; // 100 Sec.
	private final BrowserPool pool = new BrowserPool(new CrawljaxConfigurationReader(
	        new CrawljaxConfiguration()));

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
	@Test(timeout = TIMEOUT)
	@Ignore
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
	@Test(timeout = TIMEOUT)
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
	@Test(timeout = TIMEOUT)
	@Ignore
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
	@Test(timeout = TIMEOUT)
	@Ignore
	public void testMultipleBrowsers() throws ConfigurationException, InterruptedException {
		CrawlSpecification spec = new CrawlSpecification("about:blank");
		CrawljaxConfiguration cfg = new CrawljaxConfiguration();
		cfg.setCrawlSpecification(spec);
		cfg.setThreadConfiguration(new ThreadConfiguration(4));

		CrawljaxConfigurationReader reader = new CrawljaxConfigurationReader(cfg);

		BrowserPool pool = new BrowserPool(reader);

		pool.requestBrowser();
		pool.requestBrowser();
		EmbeddedBrowser b1 = pool.requestBrowser();
		pool.freeBrowser(b1);

		pool.shutdown();
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
	@Test(timeout = TIMEOUT)
	@Ignore
	public void testMultipleBrowsersFastBoot() throws ConfigurationException,
	        InterruptedException {
		CrawlSpecification spec = new CrawlSpecification("about:blank");
		CrawljaxConfiguration cfg = new CrawljaxConfiguration();
		cfg.setCrawlSpecification(spec);
		ThreadConfiguration tc = new ThreadConfiguration(4);
		cfg.setThreadConfiguration(tc);

		CrawljaxConfigurationReader reader = new CrawljaxConfigurationReader(cfg);

		BrowserPool pool = new BrowserPool(reader);

		pool.requestBrowser();
		pool.requestBrowser();
		EmbeddedBrowser b1 = pool.requestBrowser();
		pool.freeBrowser(b1);

		pool.shutdown();
	}

	/**
	 * Test opening 4 browsers, 3 requested, 1 returned. close should be done within TIMEOUT.
	 * Checking that the fast boot option is faster than the non fast boot option.
	 * 
	 * @throws ConfigurationException
	 *             when config is not correct
	 * @throws InterruptedException
	 *             when the request for a browser is interupped
	 */
	// TODO Stefan turn on again
	@Test(timeout = TIMEOUT)
	@Ignore
	public void testMultipleBrowsersFastBootIsIndeadFaster() throws ConfigurationException,
	        InterruptedException {
		CrawlSpecification spec = new CrawlSpecification("about:blank");
		CrawljaxConfiguration cfg = new CrawljaxConfiguration();
		cfg.setCrawlSpecification(spec);
		ThreadConfiguration tc = new ThreadConfiguration(4);
		cfg.setThreadConfiguration(tc);

		CrawljaxConfigurationReader reader = new CrawljaxConfigurationReader(cfg);

		long runtimeNonFastBoot = runNonFastBoot(reader);

		long runtimeFastBoot = runFastBoot(reader);

		Assert.assertTrue("Fast boot is faster", runtimeNonFastBoot > runtimeFastBoot);
	}

	private long runFastBoot(CrawljaxConfigurationReader reader) throws InterruptedException {
		long start = System.currentTimeMillis();
		BrowserPool pool = new BrowserPool(reader);

		pool.requestBrowser();
		pool.requestBrowser();
		EmbeddedBrowser b1 = pool.requestBrowser();
		pool.freeBrowser(b1);

		Thread closeThread = pool.close();

		long runtimeFastBoot = System.currentTimeMillis() - start;

		closeThread.join();
		return runtimeFastBoot;
	}

	private long runNonFastBoot(CrawljaxConfigurationReader reader) throws InterruptedException {
		long runtimeNonFastBoot = 0;
		long start = System.currentTimeMillis();
		BrowserPool pool = new BrowserPool(reader);

		pool.requestBrowser();
		pool.requestBrowser();
		EmbeddedBrowser b1 = pool.requestBrowser();
		pool.freeBrowser(b1);

		Thread closeThread = pool.close();

		runtimeNonFastBoot = System.currentTimeMillis() - start;

		closeThread.join();
		return runtimeNonFastBoot;
	}
}