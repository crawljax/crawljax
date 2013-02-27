package com.crawljax.core;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasEdges;
import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.RunWithWebServer;

@Category(BrowserTest.class)
public class PopUpTest {

	static CrawljaxController crawljax;

	@ClassRule
	public static final RunWithWebServer WEB_SERVER = new RunWithWebServer("site");

	@BeforeClass
	public static void setUpBeforeClass() throws ConfigurationException {
		CrawljaxConfigurationBuilder builder =
		        CrawljaxConfiguration.builderFor(WEB_SERVER.getSiteUrl().toExternalForm()
		                + "popup");
		builder.setMaximumDepth(3);
		builder.crawlRules().click("a");
		builder.crawlRules().waitAfterEvent(100, TimeUnit.MILLISECONDS);
		builder.crawlRules().waitAfterReloadUrl(100, TimeUnit.MILLISECONDS);
		crawljax = new CrawljaxController(builder.build());
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

}
