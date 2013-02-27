package com.crawljax.crawls;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jetty.util.resource.Resource;

import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.test.BaseCrawler;

/**
 * Wraps a Crawljax instance the crawls the simplesite.
 */
public class HiddenElementsSiteCrawl extends BaseCrawler {

	public static final int NUMBER_OF_STATES = 3;
	private final boolean clickHiddenElements;

	public HiddenElementsSiteCrawl(boolean clickHiddenElements) {
		super(Resource.newClassPathResource("/site"));
		this.clickHiddenElements = clickHiddenElements;
	}

	@Override
	protected URL getUrl() {
		try {
			return new URL(super.getUrl() + "hidden-elements-site");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected CrawljaxConfigurationBuilder newCrawlConfiguartionBuilder() {
		CrawljaxConfigurationBuilder builder =
		        super.newCrawlConfiguartionBuilder();
		builder.crawlRules().crawlHiddenAnchors(clickHiddenElements);
		return builder;
	}

}
