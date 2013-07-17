package com.crawljax.metrics;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Module;

/**
 * The module used for setting up Metrics.
 */
public class MetricsModule extends AbstractModule implements Module {

	/**
	 * The prefix for a {@link Metric} concerning Crawljax.
	 */
	public static final String CRAWL_PREFIX = "com.crawljax.crawl";

	/**
	 * The prefix for a {@link Metric} concerning the events during a crawl.
	 */
	public static final String EVENTS_PREFIX = CRAWL_PREFIX + "events.";

	/**
	 * The prefix for a {@link Metric} concerning the plugins.
	 */
	public static final String PLUGINS_PREFIX = CRAWL_PREFIX + "plugins.";

	@Override
	protected void configure() {
		bind(MetricRegistry.class).asEagerSingleton();
	}

}
