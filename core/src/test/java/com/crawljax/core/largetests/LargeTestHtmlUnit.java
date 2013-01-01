package com.crawljax.core.largetests;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.browser.EmbeddedBrowserBuilder;
import com.crawljax.browser.WebDriverBackedEmbeddedBrowser;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfigurationReader;

@Ignore("Doesnt work at the moment. Fix later")
public class LargeTestHtmlUnit extends LargeTestSuper {

	private static final int waitAfterEvent = 400;
	private static final int waitAfterReload = 400;
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

		new CrawljaxController(crawljaxConfiguration).run();

	}

}
