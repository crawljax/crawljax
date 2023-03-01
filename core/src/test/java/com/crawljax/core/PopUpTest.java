package com.crawljax.core;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasEdges;
import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.hamcrest.MatcherAssert.assertThat;

import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.RunWithWebServer;
import java.util.concurrent.TimeUnit;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(BrowserTest.class)
public class PopUpTest {

    @ClassRule
    public static final RunWithWebServer WEB_SERVER = new RunWithWebServer("site");

    @Test
    public void testPopups() throws CrawljaxException {
        CrawljaxConfigurationBuilder builder = WEB_SERVER.newConfigBuilder("popup");
        builder.setMaximumDepth(3);
        builder.crawlRules().click("a");
        builder.crawlRules().waitAfterEvent(100, TimeUnit.MILLISECONDS);
        builder.crawlRules().waitAfterReloadUrl(100, TimeUnit.MILLISECONDS);

        CrawljaxRunner runner = new CrawljaxRunner(builder.build());
        CrawlSession session = runner.call();
        assertThat(session.getStateFlowGraph(), hasEdges(3));
        assertThat(session.getStateFlowGraph(), hasStates(3));
    }
}
