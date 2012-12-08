package com.crawljax.core.largetests;

import static org.junit.Assert.fail;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.BeforeClass;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawljaxConfiguration;

public class LargeTestChrome extends LargeTestSuper {

	private static final int waitAfterEvent = 100;
	private static final int waitAfterReload = 100;
	private static BrowserType browser = BrowserType.chrome;
	private static final String INDEX = "http://spci.st.ewi.tudelft.nl/demo/testsite/";

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
