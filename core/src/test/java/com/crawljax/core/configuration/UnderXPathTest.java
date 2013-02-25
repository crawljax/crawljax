package com.crawljax.core.configuration;

import static org.junit.Assert.assertEquals;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.RunWithWebServer;

/**
 * Test case for issue number 16: http://code.google.com/p/crawljax/issues/detail?id=16
 * 
 * @author Frank Groeneveld
 */
@Category(BrowserTest.class)
public class UnderXPathTest {

	private CrawlSession session = null;

	@ClassRule
	public static final RunWithWebServer SERVER = new RunWithWebServer("site");

	@Test
	public void testDontClickUnderXPath() throws Exception {

		CrawlSpecification crawler =
		        new CrawlSpecification(SERVER.getSiteUrl() + "underxpath.html");
		CrawljaxConfiguration config = new CrawljaxConfiguration();

		crawler.click("li");
		crawler.dontClick("li").underXPath("//UL[@class=\"dontclick\"]");

		config.setCrawlSpecification(crawler);

		config.addPlugin(new PostCrawlingPlugin() {

			@Override
			public void postCrawling(CrawlSession session) {
				UnderXPathTest.this.session = session;
			}

		});

		CrawljaxController crawljax = new CrawljaxController(config);

		crawljax.run();

		assertEquals("There should be no outgoing links", 0, session.getStateFlowGraph()
		        .getOutgoingClickables(session.getInitialState()).size());
	}

	@Test
	public void testClickUnderXPath() throws ConfigurationException, CrawljaxException {

		CrawlSpecification crawler =
		        new CrawlSpecification(SERVER.getSiteUrl() + "underxpath.html");
		CrawljaxConfiguration config = new CrawljaxConfiguration();

		crawler.click("li").underXPath("//UL[@class=\"dontclick\"]");

		config.setCrawlSpecification(crawler);

		config.addPlugin(new PostCrawlingPlugin() {

			@Override
			public void postCrawling(CrawlSession session) {
				UnderXPathTest.this.session = session;
			}

		});

		CrawljaxController crawljax = new CrawljaxController(config);

		crawljax.run();

		assertEquals("There should be 2 outgoing links", 2, session.getStateFlowGraph()
		        .getOutgoingClickables(session.getInitialState()).size());
	}
}
