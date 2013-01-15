package com.crawljax.plugins.crawloverview;

import com.crawljax.core.CrawljaxException;

/**
 * Gets thrown when something unexpected goes wrong inside the {@link CrawlOverview} plugin.
 */
@SuppressWarnings("serial")
public class CrawlOverviewException extends CrawljaxException {

	public CrawlOverviewException(String message, Throwable cause) {
		super(message, cause);
	}

	public CrawlOverviewException(String message) {
		super(message);
	}

}
