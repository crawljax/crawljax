package com.crawljax.plugins.crawloverview;

import static com.crawljax.crawltests.SimpleSiteCrawl.NUMBER_OF_EDGES;
import static com.crawljax.crawltests.SimpleSiteCrawl.NUMBER_OF_STATES;
import static com.crawljax.matchers.IsValidJson.isValidJson;
import static com.crawljax.test.matchers.FileMatcher.exists;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.crawltests.SimpleSiteCrawl;
import com.crawljax.plugins.crawloverview.model.OutPutModel;
import com.crawljax.plugins.crawloverview.model.StateStatistics;
import com.crawljax.rules.TempDirInTargetFolder;

public class SimpleSiteCrawlTest {

	private static final Logger LOG = LoggerFactory
	        .getLogger(SimpleSiteCrawlTest.class);
	private static OutPutModel result;

	@ClassRule
	public static final TempDirInTargetFolder TMP_FOLDER = new TempDirInTargetFolder(
	        "simple-crawl", true);

	private static File outFolder;

	@BeforeClass
	public static void runCrawl() throws Exception {
		outFolder = TMP_FOLDER.getTempDir();
		SimpleSiteCrawl simpleCrawl = new SimpleSiteCrawl() {
			@Override
			protected CrawljaxConfigurationBuilder newCrawlConfigurationBuilder() {
				return super.newCrawlConfigurationBuilder().setOutputDirectory(
				        TMP_FOLDER.getTempDir());
			}
		};
		simpleCrawl.setup();
		CrawlOverview plugin = new CrawlOverview();
		simpleCrawl.crawlWith(plugin);
		result = plugin.getResult();
		LOG.debug("TMP folder is in {}", outFolder.getAbsoluteFile());
	}

	@Test
	public void allScreenShotsAreSaved() {
		File screenShotFolder = new File(outFolder, "screenshots");
		assertThat("Screenshot folder exists", screenShotFolder.exists(),
		        is(true));
		int screenshots = SimpleSiteCrawl.NUMBER_OF_STATES * 2;
		assertThat("Number of screenshots", screenShotFolder.list(),
		        arrayWithSize(screenshots));
	}

	@Test
	public void allStateFilesAreSaved() {
		File statesFolder = new File(outFolder, "states");
		assertThat("States folder exists", statesFolder.exists(), is(true));
		assertThat("Number of states matches", statesFolder.list(),
		        arrayWithSize(NUMBER_OF_STATES));
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
		assertThat("Least fan in", stats.getLeastFanIn().getCount(), is(1));
		assertThat("Most fan in", stats.getMostFanIn().getCount(), is(2));
		assertThat("Least fan out", stats.getLeastFanOut().getCount(), is(0));
		assertThat("Most fan out", stats.getMostFanOut().getCount(), is(2));
	}

	@Test
	public void resultFileIsWritten() {
		assertThat(new File(outFolder, "result.json"), exists());
		assertThat(new File(outFolder, "result.json"), isValidJson());
	}

	@Test
	public void configFileIsWritten() {
		assertThat(new File(outFolder, "config.json"), exists());
		assertThat(new File(outFolder, "config.json"), isValidJson());
	}
}
