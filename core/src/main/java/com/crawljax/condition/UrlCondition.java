package com.crawljax.condition;

import com.crawljax.browser.EmbeddedBrowser;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import net.jcip.annotations.Immutable;

/**
 * Conditions that returns true iff the browser's current url contains url. Note: Case insensitive
 *
 * @author dannyroest@gmail.com (Danny Roest)
 */
@Immutable
public class UrlCondition implements Condition {

	private final String url;

	/**
	 * @param url the URL.
	 */
	public UrlCondition(String url) {
		this.url = url;
	}

	@Override
	public boolean check(EmbeddedBrowser browser) {
		return browser.getCurrentUrl().toLowerCase().contains(url);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getClass(), url);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof UrlCondition) {
			UrlCondition that = (UrlCondition) object;
			return Objects.equal(this.url, that.url);
		}
		return false;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("url", url)
				.toString();
	}

}
