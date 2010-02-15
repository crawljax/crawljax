package com.crawljax.core.largetests;

import static org.junit.Assert.fail;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.BeforeClass;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawljaxConfiguration;

public class LargeFirefoxTest extends LargeTestSuper {

	private static final int waitAfterEvent = 200;
	private static final int waitAfterReload = 200;
	private static BrowserType browser = BrowserType.firefox;
	private static final String INDEX = "src/test/site/index.html";

	/**
	 * Runs crawljax.
	 * 
	 * @throws java.lang.Exception
	 *             when error while crawling
	 */
	@BeforeClass
	public static void setUpBeforeClass() {
		CrawljaxConfiguration crawljaxConfiguration = new CrawljaxConfiguration();
		// crawljaxConfiguration.setThreadConfiguration(new ThreadConfiguration(3, 3, true));
		crawljaxConfiguration.setCrawlSpecification(getCrawlSpecification("file://"
		        + new File(INDEX).getAbsolutePath(), waitAfterEvent, waitAfterReload));
		addPlugins(crawljaxConfiguration);
		crawljaxConfiguration.setBrowser(browser);
		try {
			new CrawljaxController(crawljaxConfiguration).run();
		} catch (ConfigurationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (CrawljaxException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
