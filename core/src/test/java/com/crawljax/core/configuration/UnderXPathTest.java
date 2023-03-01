package com.crawljax.core.configuration;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.hamcrest.MatcherAssert.assertThat;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.CrawlRules.CrawlRulesBuilder;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.RunWithWebServer;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Test case for issue number 16: http://code.google.com/p/crawljax/issues/detail?id=16
 */
@Category(BrowserTest.class)
public class UnderXPathTest {

    @ClassRule
    public static final RunWithWebServer SERVER = new RunWithWebServer("/site");

    @Test
    public void testDontClickUnderXPath() {
        CrawljaxConfigurationBuilder builder = SERVER.newConfigBuilder("underxpath.html");
        builder.crawlRules().click("a");
        builder.crawlRules().dontClick("a").underXPath("//A[@class=\"noClickClass\"]");

        CrawlRulesBuilder rules = builder.crawlRules();
        rules.dontClick("a").withAttribute("id", "noClickId");
        rules.dontClickChildrenOf("div").withClass("noChildrenOfClass");
        rules.dontClickChildrenOf("div").withId("noChildrenOfId");

        CrawlSession session = new CrawljaxRunner(builder.build()).call();

        assertThat(session.getStateFlowGraph(), hasStates(2));
    }
}
