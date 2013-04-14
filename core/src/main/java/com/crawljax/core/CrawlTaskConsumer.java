package com.crawljax.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.di.CoreModule.ConsumersDoneLatch;
import com.crawljax.di.CoreModule.CrawlQueue;
import com.crawljax.di.CoreModule.RunningConsumers;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;

public class CrawlTaskConsumer implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(CrawlTaskConsumer.class);

	private final BlockingQueue<CrawlTask> taskQueue;
	private final AtomicInteger runningConsumers;

	private final CountDownLatch consumersDoneLatch;

	@Inject
	CrawlTaskConsumer(@CrawlQueue BlockingQueue<CrawlTask> taskQueue,
	        @RunningConsumers AtomicInteger runningConsumers,
	        @ConsumersDoneLatch CountDownLatch consumersDoneLatch) {
		this.taskQueue = taskQueue;
		this.runningConsumers = runningConsumers;
		this.consumersDoneLatch = consumersDoneLatch;

	}

	@Override
	public void run() {
		try {
			while (!Thread.interrupted()) {
				CrawlTask crawlTask = taskQueue.take();
				int activeConsumers = runningConsumers.incrementAndGet();
				LOG.debug("There are {} active consumers", activeConsumers);
				handleTask(crawlTask);
				if (runningConsumers.decrementAndGet() == 0) {
					LOG.debug("No consumers active. Crawl is done. Shutting down...");
					consumersDoneLatch.countDown();
					return;
				}
			}
		} catch (InterruptedException e) {
			LOG.info("Consumer interrupted");
		}
	}

	@VisibleForTesting
	void handleTask(CrawlTask crawlTask) {
		LOG.debug("Handling task {}", crawlTask);

	}

}
