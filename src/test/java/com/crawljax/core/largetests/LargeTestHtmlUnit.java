package com.crawljax.core.largetests;

import static org.junit.Assert.fail;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.BeforeClass;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.browser.EmbeddedBrowserBuilder;
import com.crawljax.browser.WebDriverBackedEmbeddedBrowser;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfigurationReader;

public class LargeTestHtmlUnit extends LargeTestSuper {

	private static final int waitAfterEvent = 400;
	private static final int waitAfterReload = 400;
	private static final String INDEX = "http://spci.st.ewi.tudelft.nl/demo/testsite/";

	/**
	 * Runs crawljax.
	 * 
	 * @throws java.lang.Exception
	 *             when error while crawling
	 */
	@BeforeClass
	public static void setUpBeforeClass() {

		CrawljaxConfiguration crawljaxConfiguration = new CrawljaxConfiguration();
		crawljaxConfiguration.setCrawlSpecification(getCrawlSpecification(INDEX, waitAfterEvent,
		        waitAfterReload));
		addPlugins(crawljaxConfiguration);

		crawljaxConfiguration.setBrowserBuilder(new EmbeddedBrowserBuilder() {
			@Override
			public EmbeddedBrowser buildEmbeddedBrowser(CrawljaxConfigurationReader configuration) {
				return WebDriverBackedEmbeddedBrowser.withDriver(new HtmlUnitDriver(true));
			}
		});

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
