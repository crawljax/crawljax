package com.crawljax.condition;

import net.jcip.annotations.Immutable;

import com.crawljax.browser.EmbeddedBrowser;

/**
 * Conditions that returns true iff the browser's current url contains url. Note: Case insesitive
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
@Immutable
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
