package com.crawljax.core.largetests;

import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.ConfigurationTest;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.ThreadConfiguration;
import com.crawljax.test.BrowserTest;

@Category(BrowserTest.class)
public class LargeFirefoxTest extends LargeTestSuper {
	private static final int waitAfterEvent = 200;
	private static final int waitAfterReload = 200;
	private static BrowserType browser = BrowserType.firefox;

	/**
	 * Runs crawljax.
	 * 
	 * @throws java.lang.Exception
	 *             when error while crawling
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		CrawljaxConfiguration crawljaxConfiguration = new CrawljaxConfiguration();
		ThreadConfiguration tc = new ThreadConfiguration(2, 2, true);
		crawljaxConfiguration.setThreadConfiguration(tc);
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
