package com.crawljax.crawls;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.configuration.CrawlScope;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.state.StateVertex;
import com.crawljax.test.BaseCrawler;
import com.crawljax.test.BrowserTest;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(BrowserTest.class)
public class CrawlWithCustomScopeTest {

    @Test
    public void crawlsPagesOnlyInCustomScope() throws Exception {
        CrawlScope crawlScope = url -> url.contains("in_scope") || url.endsWith("crawlscope/index.html");
        BaseCrawler baseCrawler = new BaseCrawler("crawlscope") {
            @Override
            public CrawljaxConfigurationBuilder newCrawlConfigurationBuilder() {
                CrawljaxConfigurationBuilder builder = super.newCrawlConfigurationBuilder();
                builder.setCrawlScope(crawlScope);
                return builder;
            }
        };

        CrawlSession crawlSession = baseCrawler.crawl();

        URI baseUrl = baseCrawler.getWebServer().getSiteUrl();
        Set<String> crawledUrls = new HashSet<>();
        for (StateVertex state : crawlSession.getStateFlowGraph().getAllStates()) {
            crawledUrls.add(state.getUrl());
        }

        assertThat(
                crawledUrls,
                hasItems(
                        baseUrl + "crawlscope",
                        baseUrl + "crawlscope/in_scope.html",
                        baseUrl + "crawlscope/in_scope_inner.html"));
        assertThat(crawledUrls.size(), is(3));
    }
}
