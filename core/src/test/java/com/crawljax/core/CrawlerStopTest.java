package com.crawljax.core;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.junit.Assert.assertThat;

import org.junit.ClassRule;
import org.junit.Test;

import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.test.RunWithWebServer;

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
}
