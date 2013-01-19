package com.crawljax.plugins.crawloverview;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

public class OverviewIntegrationTest {

	@ClassRule
	public static final RunHoverCrawl HOVER_CRAWL = new RunHoverCrawl();

	@Test
	public void test() {
		Assert.assertTrue(true);
	}
}
