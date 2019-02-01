package com.crawljax.core;

import com.crawljax.core.state.StateVertex;
import com.crawljax.di.CrawlSessionProvider;

/**
 * {@link CrawljaxException} that is thrown when you call
 * {@link CrawlSessionProvider#get()} before the the initial (index)
 * {@link StateVertex} is crawled. Only after the index is crawled will the
 * {@link CrawlSession} be available.
 */
@SuppressWarnings("serial")
public class CrawlSessionNotSetupYetException extends CrawljaxException {

	public CrawlSessionNotSetupYetException() {
		super(
				"The crawl session is not yet available. Wait until the index state is crawled.");
	}
}
