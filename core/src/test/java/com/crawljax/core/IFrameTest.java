// Copyright 2010 Google Inc. All Rights Reserved.

package com.crawljax.core;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasEdges;
import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.RunWithWebServer;
import java.util.concurrent.TimeUnit;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * This abstract class is used a specification of all the iframe related tests.
 */
@Category(BrowserTest.class)
public class IFrameTest {

    @ClassRule
    public static final RunWithWebServer WEB_SERVER = new RunWithWebServer("/site");

    protected CrawljaxRunner crawljax;

    protected CrawljaxConfigurationBuilder setupConfig() {
        CrawljaxConfigurationBuilder builder = WEB_SERVER.newConfigBuilder("iframe");
        // Note: Tests fail with Chrome, use Firefox always.
        builder.setBrowserConfig(new BrowserConfiguration(EmbeddedBrowser.BrowserType.FIREFOX_HEADLESS));

        builder.crawlRules().waitAfterEvent(100, TimeUnit.MILLISECONDS);
        builder.crawlRules().waitAfterReloadUrl(100, TimeUnit.MILLISECONDS);
        builder.setMaximumDepth(3);
        builder.crawlRules().click("a");
        builder.crawlRules().click("input");

        return builder;
    }

    @Test
    public void testIFrameCrawlable() throws CrawljaxException {
        crawljax = new CrawljaxRunner(setupConfig().build());
        CrawlSession session = crawljax.call();
        assertThat(session.getStateFlowGraph(), hasEdges(23));
        assertThat(session.getStateFlowGraph(), hasStates(13));
    }

    @Test
    public void testIframeExclusions() throws CrawljaxException {
        CrawljaxConfigurationBuilder builder = setupConfig();
        builder.crawlRules().dontCrawlFrame("frame1");
        builder.crawlRules().dontCrawlFrame("sub");
        builder.crawlRules().dontCrawlFrame("frame0");
        CrawljaxConfiguration config = builder.build();
        crawljax = new CrawljaxRunner(config);
        CrawlSession session = crawljax.call();
        assertThat(session.getStateFlowGraph(), hasEdges(5));
        assertThat(session.getStateFlowGraph(), hasStates(4));
    }

    @Test
    public void testIFramesNotCrawled() throws CrawljaxException {
        CrawljaxConfigurationBuilder builder = setupConfig();
        builder.crawlRules().crawlFrames(false);
        crawljax = new CrawljaxRunner(builder.build());
        CrawlSession session = crawljax.call();
        assertThat(session.getStateFlowGraph(), hasEdges(5));
        assertThat(session.getStateFlowGraph(), hasStates(4));
    }

    @Test
    public void testIFramesWildcardsNotCrawled() throws CrawljaxException {
        CrawljaxConfigurationBuilder builder = setupConfig();

        builder.crawlRules().dontCrawlFrame("frame%");
        builder.crawlRules().dontCrawlFrame("sub");
        crawljax = new CrawljaxRunner(builder.build());
        CrawlSession session = crawljax.call();
        assertThat(session.getStateFlowGraph(), hasEdges(5));
        assertThat(session.getStateFlowGraph(), hasStates(4));
    }

    @Test
    public void testCrawlingOnlySubFrames() throws CrawljaxException {
        CrawljaxConfigurationBuilder builder = setupConfig();
        builder.crawlRules().dontCrawlFrame("frame1.frame10");
        crawljax = new CrawljaxRunner(builder.build());
        CrawlSession session = crawljax.call();
        assertEquals("Clickables", 21, session.getStateFlowGraph().getAllEdges().size());
        assertEquals("States", 12, session.getStateFlowGraph().getAllStates().size());
    }
}
