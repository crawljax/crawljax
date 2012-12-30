package com.crawljax.core.largetests;

import static org.junit.Assert.fail;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.test.BrowserTest;

@Category(BrowserTest.class)
public class LargeTestIE extends LargeTestSuper {

	private static final int waitAfterEvent = 800;
	private static final int waitAfterReload = 400;
	private static BrowserType browser = BrowserType.ie;
	private static final String INDEX = LargeTestIE.class.getResource("/site/index.html")
	        .toExternalForm();

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
		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			CrawljaxConfiguration crawljaxConfiguration = new CrawljaxConfiguration();
			crawljaxConfiguration.setCrawlSpecification(getCrawlSpecification(INDEX,
			        waitAfterEvent, waitAfterReload));
			addPlugins(crawljaxConfiguration);
			crawljaxConfiguration.setBrowser(browser);
			new CrawljaxController(crawljaxConfiguration).run();

		} else {
			fail("IE cannot be tested on this platform!");
		}
	}

}
