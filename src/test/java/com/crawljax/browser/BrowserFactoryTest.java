package com.crawljax.browser;

import org.junit.Test;

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
}
