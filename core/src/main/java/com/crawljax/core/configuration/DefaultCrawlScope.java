package com.crawljax.core.configuration;

import com.crawljax.util.UrlUtils;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import java.net.URI;

/**
 * A {@link CrawlScope} that allows to crawl only under a given domain.
 *
 * @since 5.0
 */
public class DefaultCrawlScope implements CrawlScope {

    private URI url;

    /**
     * Constructs a {@code DefaultCrawlScope} with the given URL.
     *
     * @param url the URL with allowed domain, must not be {@code null}.
     */
    public DefaultCrawlScope(URI url) {
        Preconditions.checkNotNull(url);
        this.url = url;
    }

    /**
     * Gets the URL used for scope check.
     *
     * @return the URL used for scope check.
     */
    public URI getUrl() {
        return url;
    }

    @Override
    public boolean isInScope(String url) {
        return UrlUtils.isSameDomain(url, this.url);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(url);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof DefaultCrawlScope) {
            DefaultCrawlScope that = (DefaultCrawlScope) object;
            return Objects.equal(this.url, that.url);
        }
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("url", url).toString();
    }
}
