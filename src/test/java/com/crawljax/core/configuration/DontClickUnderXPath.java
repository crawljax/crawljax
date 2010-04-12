package com.crawljax.core.configuration;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.plugin.PostCrawlingPlugin;

/**
 * Test case for issue number 16: http://code.google.com/p/crawljax/issues/detail?id=16
 * 
 * @author Frank Groeneveld
 */
public class DontClickUnderXPath {

	private static CrawlSession session = null;

	@BeforeClass
	public static void oneTimeSetUp() {
		CrawlSpecification crawler =
		        new CrawlSpecification(
		                "file://"
		                        + new File(
		                                "src/test/com/crawljax/core/configuration/dontclickunderxpath.html")
		                                .getAbsolutePath());
		CrawljaxConfiguration config = new CrawljaxConfiguration();

		crawler.click("li");
		crawler.dontClick("li").underXPath("//UL[class='dontclick']");

		config.setCrawlSpecification(crawler);

		config.addPlugin(new PostCrawlingPlugin() {

			@Override
			public void postCrawling(CrawlSession session) {
				DontClickUnderXPath.session = session;
			}

		});
	}

	@Test
	public void dontClickTest() {
		/* TODO: check number of candidate elements here, but first make beforeclass work :S */
	}
}
