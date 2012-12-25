package com.crawljax.crawljax_plugins_plugin;

import org.apache.commons.configuration.ConfigurationException;
import org.eclipse.jetty.util.resource.Resource;

import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;

public abstract class SampleCrawler {

	private final WebServer webServer;
	private CrawljaxConfiguration config;
	private CrawlSpecification crawlSpec;

	protected SampleCrawler(Resource siteBase) {
		this.webServer = new WebServer(siteBase);
		crawlSpec = new CrawlSpecification(webServer.getSiteUrl().toExternalForm());
		config = new CrawljaxConfiguration();
		config.setCrawlSpecification(crawlSpec);
	}

	public WebServer getWebServer() {
		return webServer;
	}

	public void crawl() throws ConfigurationException, CrawljaxException {
		CrawljaxController crawljax = new CrawljaxController(config);
		crawljax.run();
	}

}
