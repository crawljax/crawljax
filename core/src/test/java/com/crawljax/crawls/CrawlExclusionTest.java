package com.crawljax.crawls;

import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.resource.Resource;

import com.crawljax.condition.NotRegexCondition;
import com.crawljax.condition.NotXPathCondition;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.test.BaseCrawler;

public class CrawlExclusionTest extends BaseCrawler {

	public CrawlExclusionTest() {
		super(Resource.newClassPathResource("/site"));
	}

	@Override
	protected CrawljaxConfigurationBuilder newCrawlConfiguartionBuilder() {
		CrawljaxConfigurationBuilder builder =
		        CrawljaxConfiguration.builderFor(getUrl() + "crawlconditions");
		builder.setUnlimitedRuntime();
		builder.setUnlimitedCrawlDepth();
		builder.crawlRules().waitAfterEvent(500, TimeUnit.MILLISECONDS);
		builder.crawlRules().addCrawlCondition("Regex description",
		        new NotRegexCondition("DONT-CRAWL-THIS-STATE"));
		builder.crawlRules().addCrawlCondition(
		        "Xpath not present", new NotXPathCondition(
		                "//P[@id='noCrawl']"));
		return builder;
	}
}
