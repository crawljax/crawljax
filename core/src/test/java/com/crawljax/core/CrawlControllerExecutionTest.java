package com.crawljax.core;

import org.junit.Before;
import org.junit.Test;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.di.CoreModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class CrawlControllerExecutionTest {

	private CrawljaxConfiguration config;
	private Injector injector;
	private CrawlController controller;

	@Before
	public void setup() {
		config = CrawljaxConfiguration.builderFor("http://example.com")
		        .setBrowserConfig(new BrowserConfiguration(BrowserType.firefox, 5))
		        .build();
		injector = Guice.createInjector(new CoreModule(config));
		controller = injector.getInstance(CrawlController.class);
	}

	@Test(timeout = 60_000L)
	public void withNoConfigurationTheControllerShutsDown() {
		controller.run();
	}

}
