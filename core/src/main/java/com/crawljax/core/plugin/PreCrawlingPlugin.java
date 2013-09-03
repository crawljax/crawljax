package com.crawljax.core.plugin;

import java.net.URL;

import com.crawljax.core.configuration.CrawljaxConfiguration;

/**
 * {@link Plugin} that is called before the crawling starts and before the initial URL has been
 * loaded. This kind of plugins can be used to do for example 'once in a crawlsession' operations
 * like logging in a web application or reset the database to a 'clean' state.
 */
public interface PreCrawlingPlugin extends Plugin {

	/**
	 * Method that is called before Crawljax loads the initial {@link URL} and before the core
	 * starts crawling.
	 * 
	 * @param config
	 *            The {@link CrawljaxConfiguration} for the coming crawl.
	 */
	void preCrawling(CrawljaxConfiguration config) throws RuntimeException;
}
