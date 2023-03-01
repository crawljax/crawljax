package com.crawljax.core.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.net.URI;
import org.junit.Test;

public class DefaultCrawlScopeTest {

    private static final URI SEED = URI.create("http://localhost/");

    @Test(expected = NullPointerException.class)
    public void nullSeedDomainIsNotAllowed() throws Exception {
        new DefaultCrawlScope((URI) null);
    }

    @Test
    public void defaultCrawlScopeShouldIncludeSeedDomain() throws Exception {
        CrawlScope defaultCrawlScope = new DefaultCrawlScope(SEED);
        assertThat(defaultCrawlScope.isInScope("http://localhost/in/scope"), is(true));
    }

    @Test
    public void defaultCrawlScopeShouldNotIncludeNonSeedDomain() throws Exception {
        CrawlScope defaultCrawlScope = new DefaultCrawlScope(SEED);
        assertThat(defaultCrawlScope.isInScope("http://example.com/not/in/scope"), is(false));
    }
}
