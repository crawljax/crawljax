package com.crawljax.core;

import com.crawljax.core.state.StateVertex;
import com.crawljax.di.CrawlSessionProvider;

/**
 * {@link CrawljaxException} that is thrown when you call {@link Provider#get()} of
 * {@link CrawlSession} or {@link CrawlSessionProvider#get()} before the the initial (index)
 * {@link StateVertex} is crawled. Only after the index is crawled will the {@link CrawlSession} be
 * available.
 */
@SuppressWarnings("serial")
public class CrawlSessionNotSetupYetException extends CrawljaxException {

	public CrawlSessionNotSetupYetException() {
		super("The crawlsession is not yet availeble. Wait until the index state is crawled.");
	}
}
