package com.crawljax.core.largetests;

import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.test.BrowserTest;

@Category(BrowserTest.class)
public class LargeTestChrome extends LargeTestSuper {

	private static final int waitAfterEvent = 100;
	private static final int waitAfterReload = 100;
	private static BrowserType browser = BrowserType.chrome;
	private static final String INDEX = LargeTestChrome.class.getResource("/site/index.html")
	        .toExternalForm();

	/**
	 * Runs crawljax.
	 * 
	 * @throws java.lang.Exception
	 *             when error while crawling
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// System.setProperty("webdriver.chrome.driver",
		// "//Users//amesbah//repos//git//crawljax//chromedriver 4");
		CrawljaxConfiguration crawljaxConfiguration = new CrawljaxConfiguration();
		crawljaxConfiguration.setCrawlSpecification(getCrawlSpecification(INDEX, waitAfterEvent,
		        waitAfterReload));
		addPlugins(crawljaxConfiguration);
		crawljaxConfiguration.setBrowser(browser);
		new CrawljaxController(crawljaxConfiguration).run();

	}
}
