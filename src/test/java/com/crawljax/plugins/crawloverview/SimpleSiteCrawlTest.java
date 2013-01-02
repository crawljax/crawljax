package com.crawljax.plugins.crawloverview;

import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.crawljax.crawljax_plugins_plugin.SimpleSiteCrawl;

public class SimpleSiteCrawlTest {

	@Rule public final TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void testSimpleSiteCrawl() throws Exception {
		SimpleSiteCrawl simpleCrawl = new SimpleSiteCrawl();
		File outFolder = tempFolder.getRoot();
		simpleCrawl.setup();
		simpleCrawl.getConfig().addPlugin(new CrawlOverview(outFolder));
		simpleCrawl.crawl();

		File statesFolder = new File(outFolder, "states");
		assertThat("States folder exists", statesFolder.exists(), is(true));
		int states = SimpleSiteCrawl.NUMBER_OF_STATES;
		assertThat("Number of states matches", statesFolder.list(),
				arrayWithSize(states));

		File screenShotFolder = new File(outFolder, "screenshots");
		assertThat("Screenshot folder exists", screenShotFolder.exists(), is(true));
		assertThat("Number of states matches", screenShotFolder.list(),
				arrayWithSize(states));

	}
}
