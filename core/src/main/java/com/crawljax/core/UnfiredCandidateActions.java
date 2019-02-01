package com.crawljax.core;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawlRules.CrawlPriorityMode;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertex;
import com.crawljax.metrics.MetricsModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.Striped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;

/**
 * Contains all the {@link CandidateCrawlAction}s that still have to be fired to get a result.
 */
@Singleton
public class UnfiredCandidateActions {

	private static final Logger LOG = LoggerFactory.getLogger(UnfiredCandidateActions.class);

	private final Map<Integer, Queue<CandidateCrawlAction>> cache;
	private final BlockingQueue<Integer> statesWithCandidates;
	private final Striped<Lock> locks;
	private final Provider<StateFlowGraph> sfg;
	private final Counter crawlerLostCount;
	private final Counter unfiredActionsCount;

	@Inject UnfiredCandidateActions(BrowserConfiguration config, Provider<StateFlowGraph> sfg,
			MetricRegistry registry) {
		this.sfg = sfg;
		cache = Maps.newHashMap();
		statesWithCandidates = Queues.newLinkedBlockingQueue();
		// Every browser gets a lock.
		locks = Striped.lock(config.getNumberOfBrowsers());

		crawlerLostCount =
				registry.register(MetricsModule.EVENTS_PREFIX + "crawler_lost", new Counter());
		unfiredActionsCount =
				registry.register(MetricsModule.EVENTS_PREFIX + "unfired_actions", new Counter());
	}

	/**
	 * @param state The state you want to poll an {@link CandidateCrawlAction} for.
	 * @return The next to-be-crawled action or <code>null</code> if none available.
	 */
	CandidateCrawlAction pollActionOrNull(StateVertex state) {
		LOG.debug("Polling action for state {}", state.getName());
		Lock lock = locks.get(state.getId());
		try {
			lock.lock();
			Queue<CandidateCrawlAction> queue = cache.get(state.getId());
			if (queue == null) {
				return null;
			} else {
				CandidateCrawlAction action = queue.poll();
				if (queue.isEmpty()) {
					LOG.debug("All actions polled for state {}", state.getName());
					cache.remove(state.getId());
					removeStateFromQueue(state.getId());
					LOG.debug("There are now {} states with unfinished actions", cache.size());
				}
				return action;
			}
		} finally {
			lock.unlock();
		}

	}

	private void removeStateFromQueue(int id) {
		while (statesWithCandidates.remove(id)) {
			LOG.trace("Removed id {} from the queue", id);
		}
	}

	/**
	 * @param extract      The actions you want to add to a state.
	 * @param currentState The state you are in.
	 */
	public void addActions(ImmutableList<CandidateElement> extract, StateVertex currentState) {
		List<CandidateCrawlAction> actions = new ArrayList<>(extract.size());
		for (CandidateElement candidateElement : extract) {
			actions.add(new CandidateCrawlAction(candidateElement, EventType.click));
		}
		addActions(actions, currentState);
	}

	/**
	 * @param actions The actions you want to add to a state.
	 * @param state   The state name. This should be unique per state.
	 */
	void addActions(Collection<CandidateCrawlAction> actions, StateVertex state) {
		if (actions.isEmpty()) {
			LOG.debug("Received empty actions list. Ignoring...");
			return;
		}
		Lock lock = locks.get(state.getId());
		try {
			lock.lock();
			LOG.debug("Adding {} crawl actions for state {}", actions.size(), state.getId());
			if (cache.containsKey(state.getId())) {
				cache.get(state.getId()).addAll(actions);
			} else {
				cache.put(state.getId(), Queues.newConcurrentLinkedQueue(actions));
			}
			statesWithCandidates.add(state.getId());
			LOG.info("There are {} states with unfired actions", statesWithCandidates.size());
		} finally {
			lock.unlock();
		}

	}

	/**
	 * @return If there are any pending actions to be crawled. This method is not threadsafe and
	 * might return a stale value.
	 */
	public boolean isEmpty() {
		return statesWithCandidates.isEmpty();
	}

	/**
	 * @return A new crawl task as soon as one is ready. Until then, it blocks.
	 * @throws InterruptedException when taking from the queue is interrupted.
	 */
	public StateVertex awaitNewTask() throws InterruptedException {
		int id = statesWithCandidates.take();
		// Put it back the end of the queue. It will be removed later.
		statesWithCandidates.add(id);
		LOG.debug("New task polled for state {}", id);
		LOG.info("There are {} states with unfired actions", statesWithCandidates.size());
		return sfg.get().getById(id);
	}

	public StateVertex getOldest() {
		StateVertex oldestVertex = null;
		int oldest = -1;
		for (int id : statesWithCandidates) {
			if (oldest == -1) {
				oldest = id;
				oldestVertex = sfg.get().getById(id);
			}
			if (id < oldest) {
				oldest = id;
				oldestVertex = sfg.get().getById(id);
			}
		}

		while (true) {
			try {
				int id = statesWithCandidates.take();
				statesWithCandidates.put(id);
				if (id == oldest)
					break;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return oldestVertex;
	}

	public StateVertex getNextNonDuplicate() {
		StateVertex nextUnique = null;
		int nextUniqueId = -1;
		for (int id : statesWithCandidates) {
			StateVertex forId = sfg.get().getById(id);
			if (!forId.hasNearDuplicate()) {
				nextUnique = forId;
				nextUniqueId = id;
				break;
			}
		}

		if (nextUnique == null)
			return null;

		while (true) {
			try {
				int id = statesWithCandidates.take();
				statesWithCandidates.put(id);
				if (id == nextUniqueId)
					break;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return nextUnique;
	}

	/**
	 * @param crawlPriorityMode          the crawling modality
	 * @param crawlNearDuplicates        whether to consider near-duplicates
	 * @param delayNearDuplicateCrawling whether to postpone the crawl of near-duplicates
	 * @return A new crawl task as soon as one is ready. Until then, it blocks.
	 * @throws InterruptedException when taking from the queue is interrupted.
	 */
	public StateVertex awaitNewTaskPriority(CrawlPriorityMode crawlPriorityMode,
			boolean crawlNearDuplicates, boolean delayNearDuplicateCrawling)
			throws InterruptedException {
		/*
		 * int id = statesWithCandidates.take(); // Put it back the end of the queue. It will be
		 * removed later. statesWithCandidates.add(id); LOG.debug("New task polled for state {}",
		 * id); LOG.info("There are {} states with unfired actions", statesWithCandidates.size());
		 * StateVertex retVertex = sfg.get().getById(id);
		 */

		StateVertex retVertex = null;
		switch (crawlPriorityMode) {
			case NORMAL:
			case RANDOM:
			case SHALLOW_FIRST:
				retVertex = awaitNewTask();
				break;
			case OLDEST_FIRST:
				retVertex = getOldest();
				break;
		}

		if (!crawlNearDuplicates) {
			if (retVertex.hasNearDuplicate())
				return getNextNonDuplicate();
		}
		// TODO: Still have to handle priorities here
		if (delayNearDuplicateCrawling) {
			if (retVertex.hasNearDuplicate()) {
				StateVertex nextUnique = getNextNonDuplicate();
				if (nextUnique != null) {
					LOG.info("returning next unique state : {}", nextUnique.getName());
					return nextUnique;
				} else {
					LOG.info("All remaining states are near duplicates. Returning : {}",
							retVertex.getName());
				}
			}
		}
		return retVertex;

	}

	public void purgeActionsForState(StateVertex crawlTask) {
		Lock lock = locks.get(crawlTask.getId());
		try {
			lock.lock();
			LOG.debug("Removing tasks for target state {}", crawlTask.getName());
			removeStateFromQueue(crawlTask.getId());
			Queue<CandidateCrawlAction> removed = cache.remove(crawlTask.getId());
			if (removed != null) {
				unfiredActionsCount.inc(removed.size());
			}
		} finally {
			lock.unlock();
			crawlerLostCount.inc();
		}
	}
}
