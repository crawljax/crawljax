package com.crawljax.browser;

import static org.junit.Assert.fail;
import junit.framework.Assert;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfigurationReader;
import com.crawljax.core.configuration.ThreadConfiguration;

/**
 * This test, test the (public) operations from the BrowserFactory.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id$
 */
public class BrowserFactoryTest {
	private static final int TIMEOUT = 100000; // 100 Sec.
	private final BrowserFactory factory =
	        new BrowserFactory(new CrawljaxConfigurationReader(new CrawljaxConfiguration()));

	/**
	 * Request don't release and close the factory.
	 * 
	 * @throws InterruptedException
	 *             thrown from requestBrowser
	 */
	@Test
	public void testRequestClose() throws InterruptedException {

		factory.requestBrowser();
		factory.close();
	}

	/**
	 * Request a browser, release it and close the factory.
	 * 
	 * @throws InterruptedException
	 *             thrown from requestBrowser
	 */
	@Test
	public void testRequestReleaseClose() throws InterruptedException {
		EmbeddedBrowser b = factory.requestBrowser();
		factory.freeBrowser(b);
		factory.close();
	}

	/**
	 * Test a call to two times the close operation, after a browser request.
	 * 
	 * @throws InterruptedException
	 *             thrown from requestBrowser
	 */
	@Test(timeout = TIMEOUT)
	public void testDoubleClose() throws InterruptedException {
		factory.requestBrowser();
		factory.close();
		factory.close();

	}

	/**
	 * Test a call to close only.
	 */
	@Test(timeout = TIMEOUT)
	public void testCloseOnly() {
		factory.close();
	}

	/**
	 * Test a call to close only twice.
	 */
	@Test(timeout = TIMEOUT)
	public void testCloseOnlyTwoTimes() {
		factory.close();
		factory.close();
	}

	/**
	 * Test opening 4 browsers, 3 requested, 1 returned. close should be done within TIMEOUT.
	 * 
	 * @throws ConfigurationException
	 *             when config is not correct
	 * @throws InterruptedException
	 *             when the request for a browser is interupped
	 */
	@Test(timeout = TIMEOUT)
	public void testMultipleBrowsers() throws ConfigurationException, InterruptedException {
		CrawlSpecification spec = new CrawlSpecification("about:blank");
		CrawljaxConfiguration cfg = new CrawljaxConfiguration();
		cfg.setCrawlSpecification(spec);
		cfg.setThreadConfiguration(new ThreadConfiguration(4));

		CrawljaxConfigurationReader reader = new CrawljaxConfigurationReader(cfg);

		try {

			BrowserFactory factory = new BrowserFactory(reader);

			factory.requestBrowser();
			factory.requestBrowser();
			EmbeddedBrowser b1 = factory.requestBrowser();
			factory.freeBrowser(b1);

			factory.close();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * Test opening 4 browsers, 3 requested, 1 returned. close should be done within TIMEOUT. This
	 * time using the fast boot option
	 * 
	 * @throws ConfigurationException
	 *             when config is not correct
	 * @throws InterruptedException
	 *             when the request for a browser is interupped
	 */
	@Test(timeout = TIMEOUT)
	public void testMultipleBrowsersFastBoot() throws ConfigurationException,
	        InterruptedException {
		CrawlSpecification spec = new CrawlSpecification("about:blank");
		CrawljaxConfiguration cfg = new CrawljaxConfiguration();
		cfg.setCrawlSpecification(spec);
		ThreadConfiguration tc = new ThreadConfiguration(4);
		tc.setUseFastBooting(true);
		cfg.setThreadConfiguration(tc);

		CrawljaxConfigurationReader reader = new CrawljaxConfigurationReader(cfg);

		try {

			BrowserFactory factory = new BrowserFactory(reader);

			factory.requestBrowser();
			factory.requestBrowser();
			EmbeddedBrowser b1 = factory.requestBrowser();
			factory.freeBrowser(b1);

			factory.close();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
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
	@Test(timeout = TIMEOUT)
	public void testMultipleBrowsersFastBootIsIndeadFaster() throws ConfigurationException,
	        InterruptedException {
		CrawlSpecification spec = new CrawlSpecification("about:blank");
		CrawljaxConfiguration cfg = new CrawljaxConfiguration();
		cfg.setCrawlSpecification(spec);
		ThreadConfiguration tc = new ThreadConfiguration(4);
		tc.setUseFastBooting(false);
		cfg.setThreadConfiguration(tc);

		CrawljaxConfigurationReader reader = new CrawljaxConfigurationReader(cfg);

		long runtimeNonFastBoot = 0;
		try {
			long start = System.currentTimeMillis();
			BrowserFactory factory = new BrowserFactory(reader);

			factory.requestBrowser();
			factory.requestBrowser();
			EmbeddedBrowser b1 = factory.requestBrowser();
			factory.freeBrowser(b1);

			factory.close();
			runtimeNonFastBoot = System.currentTimeMillis() - start;
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		tc.setUseFastBooting(true);

		try {
			long start = System.currentTimeMillis();
			BrowserFactory factory = new BrowserFactory(reader);

			factory.requestBrowser();
			factory.requestBrowser();
			EmbeddedBrowser b1 = factory.requestBrowser();
			factory.freeBrowser(b1);

			factory.close();
			long runtimeFastBoot = System.currentTimeMillis() - start;
			Assert.assertTrue("Fast boot is faster", runtimeNonFastBoot > runtimeFastBoot);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}