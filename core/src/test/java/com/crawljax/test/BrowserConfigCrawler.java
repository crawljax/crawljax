package com.crawljax.test;

import org.eclipse.jetty.util.resource.Resource;

import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;

public class BrowserConfigCrawler extends BaseCrawler {

	private BrowserConfiguration browserConfig;

	public BrowserConfigCrawler(Resource webfolder) {
		super(webfolder);
	}

	public BrowserConfigCrawler(Resource webfolder, String siteExtension) {
		super(webfolder, siteExtension);
	}

	public BrowserConfigCrawler(String siteExtension) {
		super(siteExtension);
	}

	public BrowserConfigCrawler withBrowserConfig(BrowserConfiguration browserConfig) {
		this.browserConfig = browserConfig;
		return this;
	}

	@Override
	protected CrawljaxConfigurationBuilder newCrawlConfigurationBuilder() {
		CrawljaxConfigurationBuilder builder = super.newCrawlConfigurationBuilder();
		builder.setBrowserConfig(browserConfig);
		return builder;
	}
}
