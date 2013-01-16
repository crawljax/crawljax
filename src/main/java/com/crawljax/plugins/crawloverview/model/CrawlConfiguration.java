package com.crawljax.plugins.crawloverview.model;

import java.util.List;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.configuration.CrawljaxConfigurationReader;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class CrawlConfiguration {

	private final BrowserType browser;
	private final ImmutableList<String> crawlElements;
	private final ImmutableList<List<String>> filteredAttributes;
	private final String proxyConfig;

	public CrawlConfiguration(CrawlSession session) {
		CrawljaxConfigurationReader config = session.getCrawljaxConfiguration();
		browser = config.getBrowser();
		proxyConfig = config.getProxyConfiguration().toString();
		crawlElements = toStringList(config.getAllIncludedCrawlElements());
		filteredAttributes = ImmutableList.of(config.getFilterAttributeNames());
	}

	private ImmutableList<String> toStringList(List<?> elements) {
		Builder<String> list = ImmutableList.builder();
		for (Object e : elements) {
			list.add(e.toString());
		}
		return list.build();
	}

}
