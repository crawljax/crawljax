package com.crawljax.core;

import java.util.concurrent.Callable;

import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.di.CoreModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Runs crawljax given a certain {@link CrawljaxConfiguration}. Run {@link #call()} to start a
 * crawl.
 */
public class CrawlJaxRunner implements Callable<CrawlSession> {

	private CrawljaxConfiguration config;

	public CrawlJaxRunner(CrawljaxConfiguration config) {
		this.config = config;
	}

	/**
	 * Runs Crawljax with the given configuration.
	 * 
	 * @return The {@link CrawlSession} once the Crawl is done.
	 */
	@Override
	public CrawlSession call() {
		Injector injector = Guice.createInjector(new CoreModule(config));
		CrawlController controller = injector.getInstance(CrawlController.class);
		return controller.call();
	}

}
