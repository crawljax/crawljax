// Copyright 2010 Google Inc. All Rights Reserved.

package com.crawljax.core;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasEdges;
import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.RunWithWebServer;

/**
 * This abstract class is used a specification of all the iframe related tests.
 */
@Category(BrowserTest.class)
public class IFrameTest {

	protected CrawljaxRunner crawljax;

	@ClassRule
	public static final RunWithWebServer WEB_SERVER = new RunWithWebServer("/site");

	protected CrawljaxConfigurationBuilder setupConfig() {
		CrawljaxConfigurationBuilder builder =
		        CrawljaxConfiguration.builderFor(WEB_SERVER.getSiteUrl().resolve("iframe"));
		builder.crawlRules().waitAfterEvent(100, TimeUnit.MILLISECONDS);
		builder.crawlRules().waitAfterReloadUrl(100, TimeUnit.MILLISECONDS);
		builder.setMaximumDepth(3);
		builder.crawlRules().click("a");
		builder.crawlRules().click("input");

		return builder;
	}

	@Test
	public void testIFrameCrawlable() throws CrawljaxException {
		crawljax = new CrawljaxRunner(setupConfig().build());
		CrawlSession session = crawljax.call();
		assertThat(session.getStateFlowGraph(), hasEdges(13));
		assertThat(session.getStateFlowGraph(), hasStates(13));
	}

	@Test
	public void testIframeExclusions() throws CrawljaxException {
		CrawljaxConfigurationBuilder builder = setupConfig();
		builder.crawlRules().dontCrawlFrame("frame1");
		builder.crawlRules().dontCrawlFrame("sub");
		builder.crawlRules().dontCrawlFrame("frame0");
		CrawljaxConfiguration config = builder.build();
		crawljax = new CrawljaxRunner(config);
		CrawlSession session = crawljax.call();
		assertThat(session.getStateFlowGraph(), hasEdges(3));
		assertThat(session.getStateFlowGraph(), hasStates(4));
	}

	@Test
	public void testIFramesNotCrawled() throws CrawljaxException {
		CrawljaxConfigurationBuilder builder = setupConfig();
		builder.crawlRules().crawlFrames(false);
		crawljax = new CrawljaxRunner(builder.build());
		CrawlSession session = crawljax.call();
		assertThat(session.getStateFlowGraph(), hasEdges(3));
		assertThat(session.getStateFlowGraph(), hasStates(4));
	}

	@Test
	public void testIFramesWildcardsNotCrawled() throws CrawljaxException {
		CrawljaxConfigurationBuilder builder = setupConfig();

		builder.crawlRules().dontCrawlFrame("frame%");
		builder.crawlRules().dontCrawlFrame("sub");
		crawljax = new CrawljaxRunner(builder.build());
		CrawlSession session = crawljax.call();
		assertThat(session.getStateFlowGraph(), hasEdges(3));
		assertThat(session.getStateFlowGraph(), hasStates(4));
	}

	@Test
	public void testCrawlingOnlySubFrames() throws CrawljaxException {
		CrawljaxConfigurationBuilder builder = setupConfig();
		builder.crawlRules().dontCrawlFrame("frame1.frame10");
		crawljax = new CrawljaxRunner(builder.build());
		CrawlSession session = crawljax.call();
		assertEquals("Clickables", 12, session.getStateFlowGraph()
		        .getAllEdges().size());
		assertEquals("States", 12, session.getStateFlowGraph().getAllStates()
		        .size());
	}
}