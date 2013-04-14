package com.crawljax.di;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class CoreModule extends AbstractModule {

	private static final Logger LOG = LoggerFactory.getLogger(CoreModule.class);
	private CrawljaxConfiguration configuration;

	public CoreModule(CrawljaxConfiguration config) {
		this.configuration = config;
	}

	@Override
	protected void configure() {
		LOG.debug("Configuring the core module");

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
		bind(CountDownLatch.class).annotatedWith(ConsumersDoneLatch.class).toInstance(
		        new CountDownLatch(1));
	}

	@Provides
	@Singleton
	@CrawlQueue
	BlockingQueue<CrawlTask> crawlQueue() {
		LOG.debug("Creating the crawl queue");
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

	@BindingAnnotation
	@Target({ FIELD, PARAMETER, METHOD })
	@Retention(RUNTIME)
	public @interface ConsumersDoneLatch {
	}

}
