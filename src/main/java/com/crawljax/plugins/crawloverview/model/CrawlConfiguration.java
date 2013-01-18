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
	private final ImmutableList<String> filteredAttributes;
	private final String proxyConfig;

	public CrawlConfiguration(CrawlSession session) {
		CrawljaxConfigurationReader config = session.getCrawljaxConfiguration();
		browser = config.getBrowser();
		if (config.getProxyConfiguration() == null) {
			proxyConfig = "Not configured";
		} else {
			proxyConfig = config.getProxyConfiguration().toString();
		}
		crawlElements = toStringList(config.getAllIncludedCrawlElements());
		filteredAttributes = ImmutableList.copyOf(config.getFilterAttributeNames());

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

	public ImmutableList<String> getFilteredAttributes() {
		return filteredAttributes;
	}

	public String getProxyConfig() {
		return proxyConfig;
	}

}
