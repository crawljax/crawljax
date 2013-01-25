package com.crawljax.core.configuration;

import static org.hamcrest.core.IsEqual.equalTo;

import org.hamcrest.Factory;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class CrawlElementMatcher {

	/**
	 * @param edges
	 *            The number of expected edges.
	 * @return A {@link Matcher} that inspects if the number of edges.
	 */
	@Factory
	public static FeatureMatcher<CrawlElement, String> withXpath(String xPath) {
		return new FeatureMatcher<CrawlElement, String>(equalTo(xPath),
		        "CrawlElement with xPath", "xPath") {

			@Override
			protected String featureValueOf(CrawlElement actual) {
				return actual.getWithXpathExpression();
			}
		};
	}

}
