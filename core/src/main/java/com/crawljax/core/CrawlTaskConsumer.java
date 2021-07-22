package com.crawljax.core;

import com.crawljax.core.state.StateVertex;
import com.google.inject.Inject;

import org.openqa.selenium.NoSuchWindowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Consumes {@link StateVertex}s it gets from the {@link UnfiredCandidateActions}. It delegates the
 * actual browser interactions to a {@link Crawler} whom it has a 1 to 1 relation with.
 */
public class CrawlTaskConsumer implements Callable<Void> {

	private static final Logger LOG = LoggerFactory.getLogger(CrawlTaskConsumer.class);

	private final AtomicInteger runningConsumers;

	private final Crawler crawler;

	private final UnfiredFragmentCandidates candidates;

	private final ExitNotifier exitNotifier;

	@Inject CrawlTaskConsumer(UnfiredFragmentCandidates candidates,
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
					LOG.debug(
							"No consumers active and the cache is empty. Crawl is done. Shutting down...");
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
			StateVertex crawlTask = null;
			
			if(crawler.getCrawlRules().isDelayNearDuplicateCrawling()) {
				crawlTask = candidates.awaitNewTask(crawler.getContext().getCurrentState(), crawler.getOnUrlSet(), crawler.getContext().getFragmentManager());
				if(crawlTask == null) {
					LOG.info("Interrupting thread");
					Thread.currentThread().interrupt();
				}
			}
			else {
//				crawlTask = candidates
//					.awaitNewTaskPriority(crawler.getCrawlRules().getCrawlPriorityMode(),
//							crawler.getCrawlRules().isCrawlNearDuplicates(),
//							crawler.getCrawlRules().isDelayNearDuplicateCrawling(),
//							crawler.getContext().getCurrentState());
				LOG.error("Set NearDuplicateCrawling flag in crawl rules");
				System.exit(-1);
			}
			int activeConsumers = runningConsumers.incrementAndGet();
			LOG.info("There are {} active consumers", activeConsumers);
			handleTask(crawlTask);
		} catch (RuntimeException e) {
			LOG.error("Could not complete state crawl: " + e.getMessage(), e);
		}
	}

	private void handleTask(StateVertex state) throws InterruptedException {
		LOG.debug("Going to handle tasks in {}", state);
		try {
			crawler.execute(state);
		}catch(Exception ex) {
			if(ex instanceof NoSuchWindowException) {
				LOG.info("Window closed!! Stopping Crawl");
				Thread.currentThread().interrupt();
				this.exitNotifier.signalCrawlExhausted();

			}
		}
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
