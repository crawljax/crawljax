package com.crawljax.examples;

import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;

/**
 * Example of running Crawljax with a custom crawl scope.
 */
public final class CrawlScopeExample {

    private static final String URL = "http://example.com/";

    /**
     * Run this method to start the crawl.
     */
    public static void main(String[] args) {
        CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(URL);

        // Don't allow to crawl subdomains (default scope crawls subdomains).
        builder.setCrawlScope(url -> url.startsWith(URL));

        CrawljaxRunner crawljax = new CrawljaxRunner(builder.build());
        crawljax.call();
    }
}
