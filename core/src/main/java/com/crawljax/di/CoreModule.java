package com.crawljax.di;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import com.crawljax.core.CrawlTask;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawlRules;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.ProxyConfiguration;
import com.crawljax.core.plugin.Plugins;
import com.google.common.collect.Queues;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Provides;

@Slf4j
public class CoreModule extends AbstractModule {

	private CrawljaxConfiguration configuration;

	public CoreModule(CrawljaxConfiguration config) {
		this.configuration = config;
	}

	@Override
	protected void configure() {
		log.debug("Configuring the core module");

		bindConfigurations();

		bind(AtomicInteger.class).annotatedWith(RunningConsumers.class).toInstance(
		        new AtomicInteger(0));

		bind(ExecutorService.class).toInstance(Executors.newCachedThreadPool());
	}

	private void bindConfigurations() {
		bind(CrawljaxConfiguration.class).toInstance(configuration);
		bind(CrawlRules.class).toInstance(configuration.getCrawlRules());
		bind(BrowserConfiguration.class).toInstance(configuration.getBrowserConfig());
		bind(Plugins.class).toInstance(configuration.getPlugins());
		bind(ProxyConfiguration.class).toInstance(configuration.getProxyConfiguration());
	}

	@Provides
	@Singleton
	@CrawlQueue
	BlockingQueue<CrawlTask> crawlQueue() {
		log.debug("Creating the crawl queue");
		return Queues.newLinkedBlockingQueue();
	}

	@BindingAnnotation
	@Target({ FIELD, PARAMETER, METHOD })
	@Retention(RUNTIME)
	public @interface CrawlQueue {
	}

	@BindingAnnotation
	@Target({ FIELD, PARAMETER, METHOD })
	@Retention(RUNTIME)
	public @interface RunningConsumers {
	}

}
