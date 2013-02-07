package com.crawljax.crawls;

import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.resource.Resource;

import com.crawljax.condition.NotRegexCondition;
import com.crawljax.condition.NotXPathCondition;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.test.BaseCrawler;

public class CrawlExclusionTest extends BaseCrawler {

	public CrawlExclusionTest() {
		super(Resource.newClassPathResource("/site"));
	}

	@Override
	protected CrawlSpecification newCrawlSpecification() {
		CrawlSpecification spec = new CrawlSpecification(getUrl() + "crawlconditions");
		spec.clickDefaultElements();
		spec.setDepth(0);
		spec.setWaitTimeAfterEvent(500, TimeUnit.MILLISECONDS);
		spec.addCrawlCondition("Regex description",
		        new NotRegexCondition("DONT-CRAWL-THIS-STATE"));

		spec.addCrawlCondition("Xpath not present", new NotXPathCondition("//P[@id='noCrawl']"));

		return spec;
	}
}
