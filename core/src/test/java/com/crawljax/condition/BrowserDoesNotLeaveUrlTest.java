package com.crawljax.condition;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.hamcrest.MatcherAssert.assertThat;

import com.crawljax.core.CrawlSession;
import com.crawljax.test.BaseCrawler;
import com.crawljax.test.BrowserTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(BrowserTest.class)
public class BrowserDoesNotLeaveUrlTest {

    @Test
    public void whenJavaScriptNavigatesAwayFromPageItIsBlocked() {
        BaseCrawler crawler = new BaseCrawler("navigate_other_urls.html");
        // crawler.showWebSite();
        CrawlSession session = crawler.crawl();
        assertThat(session.getStateFlowGraph(), hasStates(5));
    }
}
