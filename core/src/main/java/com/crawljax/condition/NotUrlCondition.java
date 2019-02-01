package com.crawljax.condition;

import com.crawljax.browser.EmbeddedBrowser;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import net.jcip.annotations.Immutable;

/**
 * Conditions that returns true iff the browser's current url NOT contains url. Note: Case
 * insensitive.
 *
 * @author dannyroest@gmail.com (Danny Roest)
 */
@Immutable
public class NotUrlCondition implements Condition {

	private final UrlCondition urlCondition;

	/**
	 * @param url the URL.
	 */
	public NotUrlCondition(String url) {
		this.urlCondition = new UrlCondition(url);
	}

	@Override
	public boolean check(EmbeddedBrowser browser) {
		return Logic.not(urlCondition).check(browser);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getClass(), urlCondition);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof NotUrlCondition) {
			NotUrlCondition that = (NotUrlCondition) object;
			return Objects.equal(this.urlCondition, that.urlCondition);
		}
		return false;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("urlCondition", urlCondition)
				.toString();
	}

}
