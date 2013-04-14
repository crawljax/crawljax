package com.crawljax.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.Plugins;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateVertex;
import com.crawljax.di.CoreModule.ConsumersDoneLatch;
import com.crawljax.di.CoreModule.CrawlQueue;
import com.crawljax.di.CrawlSessionProvider;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Starts and shuts down the crawl.
 */
@Singleton
public class CrawlController {

	private static final Logger LOG = LoggerFactory.getLogger(CrawlController.class);

	private final Provider<CrawlTaskConsumer> consumerFactory;
	private final ExecutorService executor;
	private final BrowserConfiguration config;
	private final BlockingQueue<CrawlTask> tasks;
	private final CountDownLatch consumersDoneLatch;

	private final CrawlSessionProvider crawlSessionProvider;

	private final Plugins plugins;

	@Inject
	CrawlController(ExecutorService executor, Provider<CrawlTaskConsumer> consumerFactory,
	        CrawljaxConfiguration config, @CrawlQueue BlockingQueue<CrawlTask> tasks,
	        @ConsumersDoneLatch CountDownLatch consumersDoneLatch,
	        CrawlSessionProvider crawlSessionProvider) {
		this.executor = executor;
		this.consumerFactory = consumerFactory;
		this.config = config.getBrowserConfig();
		this.plugins = config.getPlugins();
		this.tasks = tasks;
		this.consumersDoneLatch = consumersDoneLatch;
		this.crawlSessionProvider = crawlSessionProvider;
	}

	/**
	 * Run the configured crawl.
	 */
	public void run() {
		tasks.add(initialTask());
		CrawlTaskConsumer firstConsumer = consumerFactory.get();
		StateVertex firstState = firstConsumer.crawlIndex();
		crawlSessionProvider.setup(firstState);
		plugins.runOnNewStatePlugins(crawlSessionProvider.get());
		executeConsumers(firstConsumer);
	}

	private CrawlTask initialTask() {
		Eventable event = new Eventable();
		return new CrawlTask(ImmutableList.of(event));
	}

	private void executeConsumers(CrawlTaskConsumer firstConsumer) {
		LOG.debug("Starting {} consumers", config.getNumberOfBrowsers());
		executor.execute(firstConsumer);
		for (int i = 1; i < config.getNumberOfBrowsers(); i++) {
			executor.execute(consumerFactory.get());
		}
		try {
			consumersDoneLatch.await();
		} catch (InterruptedException e) {
			LOG.warn("The crawl was interrupted before it finished. Shutting down...");
		} finally {
			shutDown();
		}
	}

	private void shutDown() {
		LOG.info("Received shutdown notice");
		executor.shutdownNow();
		if (!tasks.isEmpty()) {
			LOG.warn("The crawler got the shutdown command while it wasn't finished");
		}
		try {
			LOG.debug("Waiting for task consumers to stop...");
			executor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			LOG.warn("Interrupted before being able to shut down executor pool", e);
		}
		LOG.debug("terminated");
	}

}
