package com.crawljax.condition;

import com.crawljax.browser.EmbeddedBrowser;

/**
 * Conditions that returns true iff the browser's current url contains url. Note: Case insesitive
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id: UrlCondition.java 6301 2009-12-24 16:36:24Z mesbah $
 */
public class UrlCondition extends AbstractCondition {

	private final String url;

	/**
	 * @param url
	 *            the URL.
	 */
	public UrlCondition(String url) {
		this.url = url;
	}

	@Override
	public boolean check(EmbeddedBrowser browser) {
		return browser.getCurrentUrl().toLowerCase().contains(url);
	}

}
