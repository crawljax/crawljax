package com.crawljax.core.configuration;

import static org.junit.Assert.assertEquals;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.RunWithWebServer;

/**
 * Test case for issue number 16: http://code.google.com/p/crawljax/issues/detail?id=16
 */
@Category(BrowserTest.class)
public class UnderXPathTest {

	private static CrawlSession session = null;

	@ClassRule
	public static final RunWithWebServer SERVER = new RunWithWebServer("/configuration");

	@Test
	public void testDontClickUnderXPath() throws Exception {
		CrawljaxConfigurationBuilder builder = SERVER.newConfigBuilder("underxpath.html");
		builder.crawlRules().click("li");
		builder.crawlRules().dontClick("li").underXPath("//UL[@class=\"dontclick\"]");
		runAndSetSession(builder);

		/* test issue 16 */
		assertEquals("There should be no outgoing links", 0, session.getStateFlowGraph().getSfg()
		        .outDegreeOf(session.getInitialState()));
	}

	private void runAndSetSession(CrawljaxConfigurationBuilder builder) {
		builder.addPlugin(new PostCrawlingPlugin() {

			@Override
			public void postCrawling(CrawlSession session) {
				UnderXPathTest.session = session;
			}

		});

		CrawljaxController crawljax = new CrawljaxController(builder.build());

		crawljax.run();
	}

	@Test
	public void testClickUnderXPath() throws ConfigurationException, CrawljaxException {
		CrawljaxConfigurationBuilder builder = SERVER.newConfigBuilder("underxpath.html");
		builder.crawlRules().click("li").underXPath("//UL[@class=\"dontclick\"]");
		builder.addPlugin(new PostCrawlingPlugin() {

			@Override
			public void postCrawling(CrawlSession session) {
				UnderXPathTest.session = session;
			}

		});

		CrawljaxController crawljax = new CrawljaxController(builder.build());

		crawljax.run();

		assertEquals("There should be 2 outgoing links", 2, session.getStateFlowGraph().getSfg()
		        .outDegreeOf(session.getInitialState()));
	}
}
