package com.crawljax.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

import com.crawljax.di.CoreModule.CrawlQueue;
import com.crawljax.di.CoreModule.RunningConsumers;
import com.google.inject.Inject;
import com.google.inject.Provider;

@Slf4j
public class CrawlTaskConsumer implements Runnable {

	private BlockingQueue<CrawlTask> taskQueue;
	private AtomicInteger runningConsumers;
	private Provider<CrawlController> controller;

	@Inject
	CrawlTaskConsumer(@CrawlQueue BlockingQueue<CrawlTask> taskQueue,
	        @RunningConsumers AtomicInteger runningConsumers, Provider<CrawlController> controller) {
		this.taskQueue = taskQueue;
		this.runningConsumers = runningConsumers;
		this.controller = controller;

	}

	@Override
	public void run() {
		try {
			while (!Thread.interrupted()) {
				CrawlTask crawlTask = taskQueue.take();
				runningConsumers.incrementAndGet();
				handleTask(crawlTask);
				if (runningConsumers.decrementAndGet() == 0) {
					controller.get().shutDown();
					return;
				}
			}
		} catch (InterruptedException e) {
			log.info("Consumer interrupted");
		}
	}

	private void handleTask(CrawlTask crawlTask) {
		log.debug("Handling task {}", crawlTask);

	}

}
