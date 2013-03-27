package com.crawljax.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.state.Eventable;
import com.crawljax.di.CoreModule.CrawlQueue;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Starts and shuts down the crawl.
 */
@Slf4j
@Singleton
public class CrawlController {

	private final Provider<CrawlTaskConsumer> consumerFactory;
	private final ExecutorService executor;
	private final BrowserConfiguration config;
	private final BlockingQueue<CrawlTask> tasks;

	@Inject
	CrawlController(ExecutorService executor, Provider<CrawlTaskConsumer> consumerFactory,
	        BrowserConfiguration config, @CrawlQueue BlockingQueue<CrawlTask> tasks) {
		this.executor = executor;
		this.consumerFactory = consumerFactory;
		this.config = config;
		this.tasks = tasks;
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
		for (int i = 0; i < config.getNumberOfBrowsers(); i++) {
			executor.execute(consumerFactory.get());
		}
	}

	/**
	 * Shut down the crawl. Sends all browsers the an interrupt signal.
	 */
	public void shutDown() {
		log.info("Received shutdown notice");
		executor.shutdownNow();
		if (!tasks.isEmpty()) {
			log.warn("The crawler got the shutdown command while it wasn't finished");
		}
		try {
			log.debug("Waiting for task consumers to stop...");
			executor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.warn("Interrupted before being able to shut down executor pool");
		}
		log.debug("terminated");
	}

}
