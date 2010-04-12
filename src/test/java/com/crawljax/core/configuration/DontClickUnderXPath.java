package com.crawljax.core.configuration;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.BeforeClass;
import org.junit.Test;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.plugin.PostCrawlingPlugin;

/**
 * Test case for issue number 16: http://code.google.com/p/crawljax/issues/detail?id=16
 * 
 * @author Frank Groeneveld
 */
public class DontClickUnderXPath {

	private static CrawlSession session = null;

	@BeforeClass
	public static void beforeClass() {

		CrawlSpecification crawler =
		        new CrawlSpecification(
		                "file://"
		                        + new File(
		                                "src/test/java/com/crawljax/core/configuration/dontclickunderxpath.html")
		                                .getAbsolutePath());
		CrawljaxConfiguration config = new CrawljaxConfiguration();

		crawler.click("li");
		crawler.dontClick("li").underXPath("//UL[class=\"dontclick\"]");

		config.setCrawlSpecification(crawler);

		config.addPlugin(new PostCrawlingPlugin() {

			@Override
			public void postCrawling(CrawlSession session) {
				DontClickUnderXPath.session = session;
			}

		});

		try {
			CrawljaxController crawljax = new CrawljaxController(config);

			crawljax.run();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		} catch (CrawljaxException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void dontClickTest() {
		assertEquals("There should be no outgoing links", 0, session.getStateFlowGraph().getSfg()
		        .outDegreeOf(session.getInitialState()));
	}
}
