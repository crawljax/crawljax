package com.crawljax.plugins.crawloverview;

import static com.crawljax.crawljax_plugins_plugin.SimpleSiteCrawl.NUMBER_OF_EDGES;
import static com.crawljax.crawljax_plugins_plugin.SimpleSiteCrawl.NUMBER_OF_STATES;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.crawljax.crawljax_plugins_plugin.SimpleSiteCrawl;
import com.crawljax.plugins.crawloverview.model.OutPutModel;
import com.crawljax.plugins.crawloverview.model.StateStatistics;

public class SimpleSiteCrawlTest {

	private static OutPutModel result;
	private static File outFolder;

	@BeforeClass
	public static void runCrawl() throws Exception {
		SimpleSiteCrawl simpleCrawl = new SimpleSiteCrawl();
		outFolder = new File("/tmp/crawlout");
		if (outFolder.exists()) {
			FileUtils.deleteDirectory(outFolder);
		}
		outFolder.mkdir();
		simpleCrawl.setup();
		CrawlOverview plugin = new CrawlOverview(outFolder);
		simpleCrawl.getConfig().addPlugin(plugin);
		simpleCrawl.crawl();
		result = plugin.getResult();
	}

	@Test
	public void allScreenShotsAreSaved() {
		File screenShotFolder = new File(outFolder, "screenshots");
		assertThat("Screenshot folder exists", screenShotFolder.exists(), is(true));
		int screenshots = SimpleSiteCrawl.NUMBER_OF_STATES * 2;
		assertThat("Number of screenshots", screenShotFolder.list(),
		        arrayWithSize(screenshots));
	}

	@Test
	public void allStateFilesAreSaved() {
		File statesFolder = new File(outFolder, "states");
		assertThat("States folder exists", statesFolder.exists(), is(true));
		int states = NUMBER_OF_STATES;
		assertThat("Number of states matches", statesFolder.list(), arrayWithSize(states));
	}

	@Test
	public void allStatesAreInResult() {
		assertThat(result.getStates().keySet(), hasSize(NUMBER_OF_STATES));
	}

	@Test
	public void allEdgesAreInResult() {
		assertThat(result.getEdges(), hasSize(NUMBER_OF_EDGES));
	}

	@Test
	public void verifyFanStatistics() {
		StateStatistics stats = result.getStatistics().getStateStats();
		assertThat("Least fan in", stats.getLeastFanIn().getFanIn(), is(0));
		assertThat("Most fan in", stats.getMostFanIn().getFanIn(), is(1));
		assertThat("Least fan out", stats.getLeastFanOut().getFanIn(), is(1));
		assertThat("Most fan out", stats.getMostFanOut().getFanOut(), is(2));
	}
}
