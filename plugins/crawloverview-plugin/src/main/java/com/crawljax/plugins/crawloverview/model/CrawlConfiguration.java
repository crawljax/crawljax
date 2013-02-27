package com.crawljax.plugins.crawloverview.model;

import java.util.List;

import javax.annotation.concurrent.Immutable;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSortedSet;

/**
 * {@link Immutable} copy of the {@link CrawljaxConfiguration}.
 */
@Immutable
public class CrawlConfiguration {

	private final BrowserType browser;
	private final ImmutableList<String> crawlElements;
	private final ImmutableSortedSet<String> filteredAttributes;
	private final String proxyConfig;

	public CrawlConfiguration(CrawlSession session) {
		CrawljaxConfiguration config = session.getCrawljaxConfiguration();
		browser = config.getBrowserConfig().getBrowsertype();
		if (config.getProxyConfiguration() == null) {
			proxyConfig = "Not configured";
		} else {
			proxyConfig = config.getProxyConfiguration().toString();
		}
		crawlElements =
		        toStringList(config.getCrawlRules().getPreCrawlConfig().getIncludedElements());

		filteredAttributes = config.getCrawlRules().getPreCrawlConfig().getFilterAttributeNames();

	}

	private ImmutableList<String> toStringList(List<?> elements) {
		Builder<String> list = ImmutableList.builder();
		for (Object e : elements) {
			list.add(e.toString());
		}
		return list.build();
	}

	public BrowserType getBrowser() {
		return browser;
	}

	public ImmutableList<String> getCrawlElements() {
		return crawlElements;
	}

	public ImmutableSortedSet<String> getFilteredAttributes() {
		return filteredAttributes;
	}

	public String getProxyConfig() {
		return proxyConfig;
	}

}
