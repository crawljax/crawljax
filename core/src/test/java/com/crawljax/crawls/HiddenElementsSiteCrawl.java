package com.crawljax.crawls;

import org.eclipse.jetty.util.resource.Resource;

import com.crawljax.core.configuration.CrawlSpecification;
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
	protected CrawlSpecification newCrawlSpecification() {
		CrawlSpecification spec = new CrawlSpecification(getUrl() + "hidden-elements-site");
		spec.clickDefaultElements();
		spec.setDepth(0);
		spec.clickHiddenAnchors(clickHiddenElements);
		return spec;
	}
}
