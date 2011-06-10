package com.crawljax.core.largetests;

import static org.junit.Assert.fail;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.BeforeClass;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.ThreadConfiguration;

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
		ThreadConfiguration tc = new ThreadConfiguration(2, 2, true);
		crawljaxConfiguration.setThreadConfiguration(tc);
		crawljaxConfiguration.setCrawlSpecification(getCrawlSpecification("file://"
		        + new File(INDEX).getAbsolutePath(), waitAfterEvent, waitAfterReload));
		addPlugins(crawljaxConfiguration);
		crawljaxConfiguration.setBrowser(browser);
		CrawljaxController crawljax = null;
		try {
			crawljax = new CrawljaxController(crawljaxConfiguration);
			crawljax.run();
		} catch (ConfigurationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (CrawljaxException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} finally {
			if (crawljax != null) {
				crawljax.terminate(true);
			}
		}
	}
}
