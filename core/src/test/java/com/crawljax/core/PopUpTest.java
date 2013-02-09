package com.crawljax.core;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasEdges;
import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.junit.Assert.assertThat;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.RunWithWebServer;

@Category(BrowserTest.class)
public class PopUpTest {

	static CrawljaxController crawljax;

	@ClassRule
	public static final RunWithWebServer WEB_SERVER = new RunWithWebServer("site");

	@BeforeClass
	public static void setUpBeforeClass() throws ConfigurationException {
		CrawljaxConfiguration crawljaxConfiguration = new CrawljaxConfiguration();
		crawljaxConfiguration.setCrawlSpecification(getCrawlSpecification());
		crawljax = new CrawljaxController(crawljaxConfiguration);
	}

	@Test
	public void testPopups() throws ConfigurationException, CrawljaxException {
		try {
			crawljax.run();
			assertThat(crawljax.getSession().getStateFlowGraph(), hasEdges(2));
			assertThat(crawljax.getSession().getStateFlowGraph(), hasStates(3));
		} finally {
			crawljax.terminate(true);
		}
	}

	private static CrawlSpecification getCrawlSpecification() {
		CrawlSpecification crawler =
		        new CrawlSpecification(WEB_SERVER.getSiteUrl().toExternalForm() + "popup");
		crawler.setWaitTimeAfterEvent(100);
		crawler.setWaitTimeAfterReloadUrl(100);
		crawler.setDepth(3);
		crawler.click("a");

		return crawler;
	}

}
