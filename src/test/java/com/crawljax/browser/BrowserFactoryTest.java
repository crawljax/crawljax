package com.crawljax.browser;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.util.PropertyHelper;

/**
 * This test, test the (public) operations from the BrowserFactory.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id$
 */
public class BrowserFactoryTest {
	private static final int TIMEOUT = 10000; // 10 Sec.

	/**
	 * Request don't release and close the factory.
	 * 
	 * @throws InterruptedException
	 *             thrown from requestBrowser
	 */
	@Test
	public void testRequestClose() throws InterruptedException {
		BrowserFactory.requestBrowser();
		BrowserFactory.close();
	}

	/**
	 * Request a browser, release it and close the factory.
	 * 
	 * @throws InterruptedException
	 *             thrown from requestBrowser
	 */
	@Test
	public void testRequestReleaseClose() throws InterruptedException {
		EmbeddedBrowser b = BrowserFactory.requestBrowser();
		BrowserFactory.freeBrowser(b);
		BrowserFactory.close();
	}

	/**
	 * Test a call to two times the close operation, after a browser request.
	 * 
	 * @throws InterruptedException
	 *             thrown from requestBrowser
	 */
	@Test(timeout = TIMEOUT)
	public void testDoubleClose() throws InterruptedException {
		BrowserFactory.requestBrowser();
		BrowserFactory.close();
		BrowserFactory.close();

	}

	/**
	 * Test a call to close only.
	 */
	@Test(timeout = TIMEOUT)
	public void testCloseOnly() {
		BrowserFactory.close();
	}

	/**
	 * Test a call to close only twice.
	 */
	@Test(timeout = TIMEOUT)
	public void testCloseOnlyTwoTimes() {
		BrowserFactory.close();
		BrowserFactory.close();
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
		// TODO Stefan. when NuberOfBrowsers specified; use that in stead...
		spec.setNumberOfThreads(4);
		CrawljaxConfiguration cfg = new CrawljaxConfiguration();
		cfg.setCrawlSpecification(spec);
		PropertyHelper.init(cfg);
		BrowserFactory.requestBrowser();
		BrowserFactory.requestBrowser();
		EmbeddedBrowser b1 = BrowserFactory.requestBrowser();
		BrowserFactory.freeBrowser(b1);
		BrowserFactory.close();
	}
}