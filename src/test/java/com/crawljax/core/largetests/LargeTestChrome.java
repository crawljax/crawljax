package com.crawljax.core.largetests;

import org.junit.BeforeClass;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.test.BrowserTest;

public class LargeTestChrome extends LargeTestSuper implements BrowserTest {

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
		// TODO
		// System.setProperty("webdriver.chrome.driver",
		// "//Applications//Google Chrome.app//Contents//MacOS//Google Chrome");
		CrawljaxConfiguration crawljaxConfiguration = new CrawljaxConfiguration();
		crawljaxConfiguration.setCrawlSpecification(getCrawlSpecification(INDEX, waitAfterEvent,
		        waitAfterReload));
		addPlugins(crawljaxConfiguration);
		crawljaxConfiguration.setBrowser(browser);
		new CrawljaxController(crawljaxConfiguration).run();

	}
}
