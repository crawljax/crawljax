package com.crawljax.core;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.state.StateVertex;
import com.crawljax.di.CoreModule.ConsumersDoneLatch;
import com.crawljax.di.CoreModule.RunningConsumers;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;

/**
 * Consumes {@link CrawlTask}s it gets from the {@link CrawlQueueManager}. It delegates the actual
 * browser interactions to a {@link NewCrawler} whom it has a 1 to 1 relation with.
 */
public class CrawlTaskConsumer implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(CrawlTaskConsumer.class);

	private final AtomicInteger runningConsumers;

	private final CountDownLatch consumersDoneLatch;

	private final NewCrawler crawler;

	private final UnhandledCandidatActionCache candidates;

	@Inject
	CrawlTaskConsumer(UnhandledCandidatActionCache candidates,
	        @RunningConsumers AtomicInteger runningConsumers,
	        @ConsumersDoneLatch CountDownLatch consumersDoneLatch, NewCrawler crawler) {
		this.candidates = candidates;
		this.runningConsumers = runningConsumers;
		this.consumersDoneLatch = consumersDoneLatch;
		this.crawler = crawler;

	}

	@Override
	public void run() {
		try {
			while (!Thread.interrupted()) {
				CrawlTask crawlTask = candidates.awaitNewTask();
				int activeConsumers = runningConsumers.incrementAndGet();
				LOG.debug("There are {} active consumers", activeConsumers);
				handleTask(crawlTask);
				if (runningConsumers.decrementAndGet() == 0) {
					LOG.debug("No consumers active. Crawl is done. Shutting down...");
					consumersDoneLatch.countDown();
					break;
				}
			}
			crawler.close();
		} catch (InterruptedException e) {
			LOG.info("Consumer interrupted");
			crawler.close();
		}
	}

	@VisibleForTesting
	void handleTask(CrawlTask crawlTask) {
		LOG.debug("Handling task {}", crawlTask);
		crawler.reset();
		crawler.execute(crawlTask);
		LOG.debug("Task executed. Returning to queue polling");
	}

	/**
	 * This method calls the index state. It should be called once in order to setup the crawl.
	 * 
	 * @return The initial state.
	 */
	public StateVertex crawlIndex() {
		return crawler.crawlIndex();
	}

}
