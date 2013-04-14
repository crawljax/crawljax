package com.crawljax.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.state.Eventable;
import com.crawljax.di.CoreModule.ConsumersDoneLatch;
import com.crawljax.di.CoreModule.CrawlQueue;
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

	@Inject
	CrawlController(ExecutorService executor, Provider<CrawlTaskConsumer> consumerFactory,
	        BrowserConfiguration config, @CrawlQueue BlockingQueue<CrawlTask> tasks,
	        @ConsumersDoneLatch CountDownLatch consumersDoneLatch) {
		this.executor = executor;
		this.consumerFactory = consumerFactory;
		this.config = config;
		this.tasks = tasks;
		this.consumersDoneLatch = consumersDoneLatch;
	}

	/**
	 * Run the configured crawl.
	 */
	public void run() {
		tasks.add(initialTask());
		executeConsumers();
	}

	private CrawlTask initialTask() {
		Eventable event = new Eventable();
		return new CrawlTask(ImmutableList.of(event));
	}

	private void executeConsumers() {
		LOG.debug("Starting {} consumers");
		for (int i = 0; i < config.getNumberOfBrowsers(); i++) {
			executor.execute(consumerFactory.get());
		}
		try {
			consumersDoneLatch.await();
		} catch (InterruptedException e) {
			LOG.warn("Interrupted before being finished. Shutting down...");
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
