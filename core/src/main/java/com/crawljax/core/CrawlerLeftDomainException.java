package com.crawljax.core;

/**
 * Is thrown when the browser leaves the domain while crawling.
 */
@SuppressWarnings("serial")
public class CrawlerLeftDomainException extends CrawljaxException {

	public CrawlerLeftDomainException(String currentUrl) {
		super("Somehow we left the domain to " + currentUrl);
	}

}
