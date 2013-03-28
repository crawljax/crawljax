package com.crawljax.condition;

import com.crawljax.browser.EmbeddedBrowser;

/**
 * Conditions that returns true iff the browser's current url NOT contains url. Note: Case
 * insensitive.
 */
public class NotUrlCondition extends AbstractCondition {

	private final UrlCondition urlCondition;

	/**
	 * @param url
	 *            the URL.
	 */
	public NotUrlCondition(String url) {
		this.urlCondition = new UrlCondition(url);
	}

	@Override
	public boolean check(EmbeddedBrowser browser) {
		return Logic.not(urlCondition).check(browser);
	}

}
