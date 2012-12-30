package com.crawljax.plugins.crawloverview.example;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.crawljax.crawljax_plugins_plugin.SimpleSiteCrawl;
import com.crawljax.plugins.crawloverview.CrawlOverview;

public class SimpleSiteCrawlTest {

	@Rule
	public final TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void testSimpleSiteCrawl() throws Exception {
		SimpleSiteCrawl simpleCrawl = new SimpleSiteCrawl();
		simpleCrawl.setup();
		simpleCrawl.getConfig().addPlugin(
				new CrawlOverview(new File("/Users/alex/Downloads/overview-new")));
		simpleCrawl.crawl();		
	}
}
