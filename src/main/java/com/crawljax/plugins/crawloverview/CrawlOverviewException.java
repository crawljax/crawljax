package com.crawljax.plugins.crawloverview;

/**
 * Gets thrown when something unexpected goes wrong inside the {@link CrawlOverview} plugin.
 */
public class CrawlOverviewException extends RuntimeException {

	public CrawlOverviewException(String message, Throwable cause) {
		super(message, cause);
	}

	public CrawlOverviewException(String message) {
		super(message);
	}

}
