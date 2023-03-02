package com.crawljax.core.configuration;

/**
 * The crawl scope allows to check if a URL is or not in scope.
 *
 * <p>URLs in scope are crawled during the crawling process.
 *
 * @since 5.0
 */
@FunctionalInterface
public interface CrawlScope {

    /**
     * Tells whether or not the given URL is in scope.
     *
     * <p>Called during the crawl process, to know if the crawling process should crawl or backtrack.
     *
     * @param url the URL to check if it's in scope.
     * @return {@code true} if the given URL is in scope, {@code false} otherwise.
     */
    boolean isInScope(String url);
}
