package com.crawljax.core;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Singleton;

import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.google.common.annotations.VisibleForTesting;

@Singleton
@ThreadSafe
public class ExitNotifier {

	/**
	 * Represents the reason Crawljax stopped.
	 */
	public enum Reason {

		/**
		 * The maximum number of states is reached as defined in
		 * {@link CrawljaxConfiguration#getMaximumStates()}.
		 */
		MAX_STATES,

		/**
		 * The maximum crawl time is reached as defined in
		 * {@link CrawljaxConfiguration#getMaximumRuntime()}.
		 */
		MAX_TIME,

		/**
		 * The crawl is done.
		 */
		EXHAUSTED,

		/**
		 * The crawler quite because of an error.
		 */
		ERROR
	}

	private final CountDownLatch latch = new CountDownLatch(1);
	private final AtomicInteger states = new AtomicInteger();
	private final int maxStates;

	private Reason reason;

	public ExitNotifier(int maxStates) {
		this.maxStates = maxStates;
	}

	/**
	 * Waits until the crawl has to stop.
	 * 
	 * @throws InterruptedException
	 *             When the wait is interrupted.
	 */
	public Reason awaitTermination() throws InterruptedException {
		latch.await();
		return reason;
	}

	/**
	 * @return The new number of states.
	 */
	public int incrementNumberOfStates() {
		int count = states.incrementAndGet();
		if (count == maxStates) {
			reason = Reason.MAX_STATES;
			latch.countDown();
		}
		return count;
	}

	public void signalTimeIsUp() {
		reason = Reason.MAX_TIME;
		latch.countDown();
	}

	/**
	 * Signal that all {@link CrawlTaskConsumer}s are done.
	 */
	public void signalCrawlExhausted() {
		reason = Reason.EXHAUSTED;
		latch.countDown();
	}

	@VisibleForTesting
	boolean isExitCalled() {
		return latch.getCount() == 0;
	}
}
