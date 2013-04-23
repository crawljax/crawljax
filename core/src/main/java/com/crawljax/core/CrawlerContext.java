package com.crawljax.core;

import javax.inject.Inject;
import javax.inject.Provider;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.state.StateVertex;

/**
 * A context for each {@link Crawler} that can be handed to the plugins.
 */
public class CrawlerContext {

	private EmbeddedBrowser browser;
	private Provider<CrawlSession> sessionProvider;
	private CrawljaxConfiguration config;

	@Inject
	public CrawlerContext(EmbeddedBrowser browser, CrawljaxConfiguration config,
	        Provider<CrawlSession> sessionProvider) {
		this.browser = browser;
		this.config = config;
		this.sessionProvider = sessionProvider;
	}

	public EmbeddedBrowser getBrowser() {
		return browser;
	}

	public CrawlSession getSession() {
		return sessionProvider.get();
	}

	public CrawljaxConfiguration getConfig() {
		return config;
	}

}
