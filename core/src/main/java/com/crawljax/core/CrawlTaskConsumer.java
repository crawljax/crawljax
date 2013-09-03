package com.crawljax.core;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.state.StateVertex;
import com.google.inject.Inject;

/**
 * Consumes {@link StateVertex}s it gets from the {@link UnfiredCandidateActions}. It delegates the
 * actual browser interactions to a {@link Crawler} whom it has a 1 to 1 relation with.
 */
public class CrawlTaskConsumer implements Callable<Void> {

	private static final Logger LOG = LoggerFactory.getLogger(CrawlTaskConsumer.class);

	private final AtomicInteger runningConsumers;

	private final Crawler crawler;

	private final UnfiredCandidateActions candidates;

	private final ExitNotifier exitNotifier;

	@Inject
	CrawlTaskConsumer(UnfiredCandidateActions candidates,
	        ExitNotifier exitNotifier, Crawler crawler) {
		this.candidates = candidates;
		this.exitNotifier = exitNotifier;
		this.crawler = crawler;
		this.runningConsumers = new AtomicInteger(0);

	}

	@Override
	public Void call() {
		try {
			while (!Thread.interrupted()) {
				if (runningConsumers.get() == 0 && candidates.isEmpty()) {
					LOG.debug("No consumers active and the cache is empty. Crawl is done. Shutting down...");
					exitNotifier.signalCrawlExhausted();
					break;
				}
				pollAndHandleCrawlTasks();
				runningConsumers.decrementAndGet();
			}
			crawler.close();
		} catch (InterruptedException e) {
			LOG.debug("Consumer interrupted");
			crawler.close();
		} catch (RuntimeException e) {
			LOG.error("Unexpected error " + e.getMessage(), e);
			throw e;
		}
		return null;
	}

	private void pollAndHandleCrawlTasks() throws InterruptedException {
		try {
			LOG.debug("Awaiting task");
			StateVertex crawlTask = candidates.awaitNewTask();
			int activeConsumers = runningConsumers.incrementAndGet();
			LOG.debug("There are {} active consumers", activeConsumers);
			handleTask(crawlTask);
		} catch (RuntimeException e) {
			LOG.error("Cound not complete state crawl: " + e.getMessage(), e);
		}
	}

	private void handleTask(StateVertex state) {
		LOG.debug("Going to handle tasks in {}", state);
		crawler.execute(state);
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

	public CrawlerContext getContext() {
		return crawler.getContext();
	}

}
