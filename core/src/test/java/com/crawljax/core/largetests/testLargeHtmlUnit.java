package com.crawljax.core.largetests;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.BeforeClass;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.ConfigurationTest;
import com.crawljax.core.configuration.CrawljaxConfiguration;

public class testLargeHtmlUnit extends LargeTestSuper {

	private static final int waitAfterEvent = 400;
	private static final int waitAfterReload = 400;
	private static BrowserType browser = BrowserType.htmlunit;

	/**
	 * Runs crawljax.
	 * 
	 * @throws CrawljaxException
	 * @throws ConfigurationException
	 * @throws java.lang.Exception
	 *             when error while crawling
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws ConfigurationException, CrawljaxException {

		CrawljaxConfiguration crawljaxConfiguration = new CrawljaxConfiguration();
		String url = ConfigurationTest.class.getResource("/site/index.html").toExternalForm();
		crawljaxConfiguration.setCrawlSpecification(getCrawlSpecification(url, waitAfterEvent,
		        waitAfterReload));
		addPlugins(crawljaxConfiguration);
		crawljaxConfiguration.setBrowser(browser);
		CrawljaxController crawljax = null;
		try {
			crawljax = new CrawljaxController(crawljaxConfiguration);
			crawljax.run();
		} finally {
			if (crawljax != null) {
				crawljax.terminate(true);
			}
		}
	}

}
