package com.crawljax.core;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.RunWithWebServer;

@Category(BrowserTest.class)
public class CrawlerStopTest {

	@ClassRule
	public static final RunWithWebServer SERVER = new RunWithWebServer("/site");

	@Test
	public void maximumDepthIsOblidged() throws Exception {
		CrawljaxConfigurationBuilder builder = SERVER.newConfigBuilder("infinite.html");
		int depth = 3;

		CrawlSession session = new CrawljaxRunner(builder.setMaximumDepth(depth).build()).call();

		assertThat(session.getStateFlowGraph(), hasStates(depth + 1));
	}

	@Test(timeout = 60_000)
	public void maximumTimeIsOblidged() throws Exception {
		CrawljaxConfigurationBuilder builder = SERVER.newConfigBuilder("infinite.html");

		new CrawljaxRunner(builder.setUnlimitedCrawlDepth()
		        .setMaximumRunTime(25, TimeUnit.SECONDS)
		        .build())
		        .call();

	}

}
