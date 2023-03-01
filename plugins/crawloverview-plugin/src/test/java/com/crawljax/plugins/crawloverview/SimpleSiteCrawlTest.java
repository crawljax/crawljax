package com.crawljax.plugins.crawloverview;

import static com.crawljax.matchers.IsValidJson.isValidJson;
import static com.crawljax.test.SimpleSiteCrawl.NUMBER_OF_EDGES;
import static com.crawljax.test.SimpleSiteCrawl.NUMBER_OF_STATES;
import static com.crawljax.test.matchers.FileMatcher.exists;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.hamcrest.core.Is.is;

import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.plugins.crawloverview.model.OutPutModel;
import com.crawljax.plugins.crawloverview.model.StateStatistics;
import com.crawljax.test.SimpleSiteCrawl;
import com.crawljax.test.rules.TempDirInTargetFolder;
import java.io.File;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleSiteCrawlTest {

    @ClassRule
    public static final TempDirInTargetFolder TMP_FOLDER = new TempDirInTargetFolder("simple-crawl", true);

    private static final Logger LOG = LoggerFactory.getLogger(SimpleSiteCrawlTest.class);
    private static OutPutModel result;
    private static File outFolder;
    private final String generatedFilesFolder = "localhost/crawl0/";

    @BeforeClass
    public static void runCrawl() {
        outFolder = TMP_FOLDER.getTempDir();
        SimpleSiteCrawl simpleCrawl = new SimpleSiteCrawl() {
            @Override
            protected CrawljaxConfigurationBuilder newCrawlConfigurationBuilder() {
                return super.newCrawlConfigurationBuilder().setOutputDirectory(TMP_FOLDER.getTempDir());
            }
        };
        simpleCrawl.setup();
        CrawlOverview plugin = new CrawlOverview();
        simpleCrawl.crawlWith(plugin);
        result = plugin.getResult();
        LOG.debug("TMP folder is in {}", outFolder.getAbsoluteFile());
    }

    @Test
    @Ignore
    public void allScreenShotsAreSaved() {
        File screenShotFolder = new File(outFolder, generatedFilesFolder + "screenshots");
        assertThat("Screenshot folder exists", screenShotFolder.exists(), is(true));
        int screenshots = SimpleSiteCrawl.NUMBER_OF_STATES * 2;
        assertThat("Number of screenshots", screenShotFolder.list(), arrayWithSize(screenshots));
    }

    @Test
    public void allStateFilesAreSaved() {
        File statesFolder = new File(outFolder, generatedFilesFolder + "states");
        assertThat("States folder exists", statesFolder.exists(), is(true));
        assertThat("Number of states matches", statesFolder.list(), arrayWithSize(NUMBER_OF_STATES));
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
        assertThat("Most fan in", stats.getMostFanIn().getCount(), is(3));
        assertThat("Least fan out", stats.getLeastFanOut().getCount(), is(1));
        assertThat("Most fan out", stats.getMostFanOut().getCount(), is(2));
    }

    @Test
    public void resultFileIsWritten() {
        assertThat(new File(outFolder, generatedFilesFolder + "result.json"), exists());
        assertThat(new File(outFolder, generatedFilesFolder + "result.json"), isValidJson());
    }

    @Test
    public void configFileIsWritten() {
        assertThat(new File(outFolder, generatedFilesFolder + "config.json"), exists());
        assertThat(new File(outFolder, generatedFilesFolder + "config.json"), isValidJson());
    }
}
