package com.crawljax.crawls;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.hamcrest.MatcherAssert.assertThat;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.BrowserOptions;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.test.BaseCrawler;
import com.crawljax.test.BrowserTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(BrowserTest.class)
public class CrawlClickablesTest {

    /**
     * Clickable detector should detect the div with event handler and perform the action to discover
     * second state
     */
    @Test
    public void testCrawlWithClickableDetection() {
        CrawlSession crawl = new BaseCrawler("clickable") {
            @Override
            public CrawljaxConfigurationBuilder newCrawlConfigurationBuilder() {
                CrawljaxConfigurationBuilder builder = super.newCrawlConfigurationBuilder();
                builder.crawlRules().clickElementsWithClickEventHandler();
                builder.crawlRules().clickOnce(true);
                BrowserOptions options = new BrowserOptions();
                options.setUSE_CDP(true);
                BrowserConfiguration browserConfiguration =
                        new BrowserConfiguration(EmbeddedBrowser.BrowserType.CHROME_HEADLESS, 1, options);
                builder.setBrowserConfig(browserConfiguration);
                return builder;
            }
        }.crawl();

        StateFlowGraph stateFlowGraph = crawl.getStateFlowGraph();

        int numStates = 2;
        assertThat(stateFlowGraph, hasStates(numStates));
    }
}
