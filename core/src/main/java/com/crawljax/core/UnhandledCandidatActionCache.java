package com.crawljax.core;

import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.di.CoreModule.CrawlQueue;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.Striped;

@Singleton
public class UnhandledCandidatActionCache {

	private static final Logger LOG = LoggerFactory.getLogger(UnhandledCandidatActionCache.class);

	private final Map<String, Queue<CandidateCrawlAction>> cache;
	private Striped<Lock> locks;

	private final BlockingQueue<CrawlTask> taskQueue;

	UnhandledCandidatActionCache(BrowserConfiguration config,
	        @CrawlQueue BlockingQueue<CrawlTask> taskQueue) {
		this.taskQueue = taskQueue;
		cache = Maps.newHashMap();

		// Every browser gets a lock.
		locks = Striped.lock(config.getNumberOfBrowsers());
	}

	/**
	 * @param state
	 *            The state you want to poll an {@link CandidateCrawlAction} for.
	 * @return The next to-be-crawled action or <code>null</code> if none available.
	 */
	CandidateCrawlAction pollActionOrNull(String state) {
		Lock lock = locks.get(state);
		try {
			lock.lock();
			Queue<CandidateCrawlAction> queue = cache.get(state);
			if (queue == null) {
				return null;
			} else {
				CandidateCrawlAction action = queue.poll();
				if (action == null) {
					LOG.debug("All actions polled for state {}", state);
					cache.remove(queue);
					LOG.debug("There are now {} states with unfinished actions", cache.size());
				}
				return action;
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * @param actions
	 *            The actions you want to add to a state.
	 * @param state
	 *            The state name. This should be unique per state.
	 */
	void addActions(Collection<CandidateCrawlAction> actions, String state) {
		Lock lock = locks.get(state);
		try {
			lock.lock();
			if (cache.containsKey(state)) {
				cache.get(state).addAll(actions);
			} else {
				cache.put(state, Queues.newConcurrentLinkedQueue(actions));
			}
		} finally {
			lock.unlock();
		}
	}
}
