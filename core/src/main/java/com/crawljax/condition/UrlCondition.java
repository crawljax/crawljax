package com.crawljax.condition;

import net.jcip.annotations.Immutable;

import com.crawljax.browser.EmbeddedBrowser;
import com.google.common.base.Objects;

/**
 * Conditions that returns true iff the browser's current url contains url. Note: Case insesitive
 * 
 * @author dannyroest@gmail.com (Danny Roest)
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

	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), url);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof UrlCondition) {
			if (!super.equals(object))
				return false;
			UrlCondition that = (UrlCondition) object;
			return Objects.equal(this.url, that.url);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
		        .add("super", super.toString())
		        .add("url", url)
		        .toString();
	}

}
