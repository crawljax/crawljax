package com.crawljax.core.configuration;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.test.BrowserTest;

/**
 * Test case for issue number 16: http://code.google.com/p/crawljax/issues/detail?id=16
 * 
 * @author Frank Groeneveld
 */
@Category(BrowserTest.class)
public class UnderXPathTest {

	private static CrawlSession session = null;

	private static final String FILENAME = "src/test/resources/configuration/underxpath.html";

	@Test
	public void testDontClickUnderXPath() throws Exception {

		CrawlSpecification crawler =
		        new CrawlSpecification("file://" + new File(FILENAME).getAbsolutePath());
		CrawljaxConfiguration config = new CrawljaxConfiguration();

		crawler.click("li");
		crawler.dontClick("li").underXPath("//UL[@class=\"dontclick\"]");

		config.setCrawlSpecification(crawler);

		config.addPlugin(new PostCrawlingPlugin() {

			@Override
			public void postCrawling(CrawlSession session) {
				UnderXPathTest.session = session;
			}

		});

		CrawljaxController crawljax = new CrawljaxController(config);

		crawljax.run();

		/* test issue 16 */
		assertEquals("There should be no outgoing links", 0, session.getStateFlowGraph().getSfg()
		        .outDegreeOf(session.getInitialState()));
	}

	@Test
	public void testClickUnderXPath() throws ConfigurationException, CrawljaxException {

		CrawlSpecification crawler =
		        new CrawlSpecification("file://" + new File(FILENAME).getAbsolutePath());
		CrawljaxConfiguration config = new CrawljaxConfiguration();

		crawler.click("li").underXPath("//UL[@class=\"dontclick\"]");

		config.setCrawlSpecification(crawler);

		config.addPlugin(new PostCrawlingPlugin() {

			@Override
			public void postCrawling(CrawlSession session) {
				UnderXPathTest.session = session;
			}

		});

		CrawljaxController crawljax = new CrawljaxController(config);

		crawljax.run();

		assertEquals("There should be 2 outgoing links", 2, session.getStateFlowGraph().getSfg()
		        .outDegreeOf(session.getInitialState()));
	}
}
