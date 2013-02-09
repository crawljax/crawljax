// Copyright 2010 Google Inc. All Rights Reserved.

package com.crawljax.core;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.experimental.categories.Category;

import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.RunWithWebServer;

/**
 * This abstract class is used a specification of all the iframe related tests.
 */
@Category(BrowserTest.class)
public abstract class IFrameSuper {

	protected CrawljaxController crawljax;

	@ClassRule
	public static final RunWithWebServer WEB_SERVER = new RunWithWebServer("/site");

	@Before
	public void setUpBeforeClass() throws ConfigurationException {
		CrawljaxConfiguration crawljaxConfiguration = new CrawljaxConfiguration();
		crawljaxConfiguration.setCrawlSpecification(getCrawlSpecification());
		crawljax = new CrawljaxController(crawljaxConfiguration);
	}

	protected CrawlSpecification getCrawlSpecification() {
		CrawlSpecification crawler =
		        new CrawlSpecification(WEB_SERVER.getSiteUrl().toExternalForm() + "iframe");
		crawler.setWaitTimeAfterEvent(100);
		crawler.setWaitTimeAfterReloadUrl(100);
		crawler.setDepth(3);
		crawler.click("a");
		crawler.click("input");

		return crawler;
	}
}