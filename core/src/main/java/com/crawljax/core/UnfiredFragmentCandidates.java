package com.crawljax.core;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawlRules;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateMachine;
import com.crawljax.core.state.StateVertex;
import com.crawljax.forms.FormInput;
import com.crawljax.fragmentation.FragmentManager;
import com.crawljax.metrics.MetricsModule;
import com.crawljax.stateabstractions.hybrid.HybridStateVertexImpl;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.Striped;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains all the {@link CandidateCrawlAction}s that still have to be fired to get a result.
 */
@Singleton
public class UnfiredFragmentCandidates {

    private static final Logger LOG = LoggerFactory.getLogger(UnfiredFragmentCandidates.class);

    private static int MAX_REPEAT = 2;

    private final Map<Integer, List<CandidateCrawlAction>> cache;
    private final BlockingQueue<Integer> statesWithCandidates;
    private final Striped<Lock> locks;
    private final Provider<StateFlowGraph> sfg;
    private final Counter crawlerLostCount;
    private final Counter unfiredActionsCount;
    private final Map<Integer, List<CandidateCrawlAction>> unreachableCache;

    //	private StateVertex nextBestState = null;
    private boolean skipExploredActions = true;
    private final List<CandidateCrawlAction> skipInputs;
    private final List<Eventable> skipInputsForPath;
    private final Map<Long, List<FormInput>> inputMap = new HashMap<>();
    private final boolean applyNonSelAdvantage;
    private final ReadWriteLock consumersStateLock;
    private final Lock consumersWriteLock;
    private final Lock consumersReadLock;
    private int runningConsumers;
    private int pendingStates;

    private boolean unexploredStates = true;

    private boolean restoreConnectedEdges = false;

    @Inject
    UnfiredFragmentCandidates(
            BrowserConfiguration config, Provider<StateFlowGraph> sfg, MetricRegistry registry, CrawlRules crawlRules) {
        this.sfg = sfg;
        cache = Maps.newHashMap();
        unreachableCache = Maps.newHashMap();
        skipInputs = new ArrayList<>();
        skipInputsForPath = new ArrayList<>();
        statesWithCandidates = Queues.newLinkedBlockingQueue();
        // Every browser gets a lock.
        locks = Striped.lock(config.getNumberOfBrowsers());

        crawlerLostCount = registry.register(MetricsModule.EVENTS_PREFIX + "crawler_lost", new Counter());
        unfiredActionsCount = registry.register(MetricsModule.EVENTS_PREFIX + "unfired_actions", new Counter());

        applyNonSelAdvantage = crawlRules.isApplyNonSelAdvantage();
        skipExploredActions = crawlRules.isSkipExploredActions();
        MAX_REPEAT = crawlRules.getMaxRepeatExploredActions();
        restoreConnectedEdges = crawlRules.isRestoreConnectedEdges();

        consumersStateLock = new ReentrantReadWriteLock();
        consumersWriteLock = consumersStateLock.writeLock();
        consumersReadLock = consumersStateLock.readLock();
    }

    private CandidateCrawlAction getBestAction(
            List<CandidateCrawlAction> availableActions, StateVertex state, FragmentManager fragmentManager) {
        if (state.getRootFragment() != null && !state.getRootFragment().isAccessTransferred()) {
            fragmentManager.setAccess(state);
        }
        long start = System.currentTimeMillis();
        boolean unexploredActionFound = false;
        double maxInfluence = 0.0;
        double maxExploredInfluence = 0.0;
        CandidateCrawlAction bestExploredAction = null;
        CandidateCrawlAction bestAction = null;
        try {
            for (CandidateCrawlAction action : availableActions) {
                CandidateElement element = action.getCandidateElement();
                if (element.isDirectAccess() || element.getEquivalentAccess() >= MAX_REPEAT) {
                    continue;
                }
                if (unexploredActionFound) {
                    if (element.wasExplored()) {
                        continue;
                    }
                }

                double influence = fragmentManager.calculateCandidateInfluence(element);
                double duplicationFactor = fragmentManager.calculateDuplicationFactor(element, state);
                if (!element.wasExplored()) {
                    unexploredActionFound = true;
                    if (influence * duplicationFactor > maxInfluence) {
                        maxInfluence = influence * duplicationFactor;
                        bestAction = action;
                    }
                } else {
                    if (influence * duplicationFactor > maxExploredInfluence) {
                        maxExploredInfluence = influence * duplicationFactor;
                        bestExploredAction = action;
                    }
                }
            }
            if (bestAction != null) {
                LOG.info("best {}", bestAction);
            } else {
                LOG.info("already explored {}", bestExploredAction);
            }
            return (bestAction != null) ? bestAction : bestExploredAction;
        } catch (Exception ex) {
            LOG.error("Error retrieving best action. Returning null...");
            LOG.debug(ex.getMessage());
        } finally {
            long end = System.currentTimeMillis();
            LOG.info("Time taken to find Best Action : " + (end - start) + " millis");
        }
        return null;
    }

    CandidateCrawlAction pollActionOrNull(
            StateMachine stateMachine, FragmentManager fragmentManager, boolean afterBacktrack) {
        StateVertex state = stateMachine.getCurrentState();

        if (!(state instanceof HybridStateVertexImpl)) {
            return pollActionOrNull(state);
        }

        StateVertex bestState = null;
        CandidateCrawlAction bestAction = null;

        LOG.debug("Polling action for state {}", state.getName());
        try {

            if (unreachableCache.get(state.getId()) != null) {
                rediscoveredState(state);
            }
            List<CandidateCrawlAction> queue = cache.get(state.getId());

            if (queue == null) {
                return bestAction;
            }

            try {
                bestAction = getBestAction(queue, state, fragmentManager);
            } catch (Exception ignored) {

            }
            if (bestAction != null) {
                CandidateElement element = bestAction.getCandidateElement();

                if (element.wasExplored()) {
                    // We found an explored action as the best action. Make sure no better option than this for now.
                    //					StateVertex bestState = fragmentManager.getClosestUnexploredFragment(state,
                    // stateMachine.getOnURLSet(), statesWithCandidates);
                    if (unexploredStates && !afterBacktrack) {
                        try {
                            bestState = fragmentManager.getClosestUnexploredState(
                                    state, stateMachine.getOnURLSet(), statesWithCandidates, applyNonSelAdvantage);
                        } catch (Exception ex) {
                            LOG.error("Error getting closest unexplored state", ex.getMessage());
                        }
                    } else {
                        // No unexplored states. Check if the number of access is less than threshold
                        //						if(element.getEquivalentAccess() > MAX_REPEAT) {
                        //							LOG.info("element already explored more than max repeat", element);
                        //							bestAction = null;
                        //						}
                        //						else {
                        LOG.info("element already explored but only {} times", element.getEquivalentAccess());
                        bestState = state;
                        //						}
                    }
                    if (bestState == null) {
                        LOG.info("No more prioritization possible?");
                    } else if (bestState.getId() != state.getId()) {
                        if (skipExploredActions) {
                            LOG.info("best action has been explored already. So purging the state!!");
                            queue.clear();
                        }
                        bestAction = null;
                        LOG.info("No unexplored elements available!!. So switching to best state: "
                                + bestState.getName());
                    }
                } else {
                    bestState = state;
                }
            } else {
                // FIFO order when best action not given by prioritization
                LOG.info("No actions available. So purging {} ", state.getName());

                queue.clear();
            }

            if (bestAction != null) {
                queue.remove(bestAction);
                if ((skipExploredActions && bestAction.getCandidateElement().wasExplored())
                        || (bestAction.getCandidateElement().getEquivalentAccess() >= MAX_REPEAT)) {
                    LOG.info("best action has been explored already. So purging the state!!");
                    LOG.info("{}", bestAction);
                    queue.clear();
                    bestAction = null;
                } else {
                    // Can record access here if needed
                }
            }

            if (queue.isEmpty()) {
                LOG.debug("All actions polled for state {}", state.getName());
                cache.remove(state.getId());
                removeStateFromQueue(state.getId());
                LOG.debug("There are now {} states with unfinished actions", cache.size());
            }
            return bestAction;

        } finally {
            if (bestState != null && bestAction != null) {
                fragmentManager.seenState(bestState);
            }
        }
    }

    void rediscoveredState(StateVertex state) {
        restoreState(state);
        if (restoreConnectedEdges) {
            ImmutableSet<StateVertex> connectedStates = sfg.get().getOutgoingStates(state);
            for (StateVertex connectedState : connectedStates) {
                LOG.info(
                        "Restoring connected {} because {} is rediscovered", connectedState.getName(), state.getName());
                restoreState(connectedState);
            }
        }
    }

    private void restoreState(StateVertex state) {
        if (unreachableCache.containsKey(state.getId())) {
            // If the actions are not in QUEUE, check if the state has been updated and lost during crawl.
            // If so, then place its actions back in the queue.
            LOG.info("Placing {} back in queue as it is rediscovered", state.getName());
            List<CandidateCrawlAction> removed = unreachableCache.get(state.getId());
            addActions(removed, state);
            unfiredActionsCount.dec(removed.size());
            unreachableCache.remove(state.getId());
            if (state.hasUnexploredActions()) {
                LOG.info("Rediscovered state{} has unexplored actions", state.getId());
                if (!unexploredStates) {
                    LOG.info("Unexplored states available again because of {}", state.getName());
                    unexploredStates = true;
                }
            }
        }
    }

    private void removeStateFromQueue(int id) {
        consumersWriteLock.lock();
        try {
            while (statesWithCandidates.remove(id)) {
                LOG.trace("Removed id {} from the queue", id);
                pendingStates--;
            }
            LOG.debug("statesWithCandidates={}", statesWithCandidates);
        } finally {
            consumersWriteLock.unlock();
        }
    }

    /**
     * @param extract      The actions you want to add to a state.
     * @param currentState The state you are in.
     */
    public void addActions(ImmutableList<CandidateElement> extract, StateVertex currentState) {
        List<CandidateCrawlAction> actions = new ArrayList<>(extract.size());
        for (CandidateElement candidateElement : extract) {
            // TODO: event type .. not always click
            EventType type = candidateElement.getEventType();
            actions.add(new CandidateCrawlAction(candidateElement, type));
        }
        addActions(actions, currentState);
    }

    /**
     * @param actions The actions you want to add to a state.
     * @param state   The state name. This should be unique per state.
     */
    void addActions(List<CandidateCrawlAction> actions, StateVertex state) {
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
                cache.put(state.getId(), actions);
            }
        } finally {
            lock.unlock();
        }

        consumersWriteLock.lock();
        try {
            addPendingState(state);
        } finally {
            consumersWriteLock.unlock();
        }
    }

    private void addPendingState(StateVertex state) {
        pendingStates++;
        statesWithCandidates.add(state.getId());
        LOG.info("There are {} states with unfired actions: {}", pendingStates, statesWithCandidates);
    }

    /**
     * @return If there are any pending actions to be crawled (and no state is being crawled).
     */
    public boolean isEmpty() {
        consumersReadLock.lock();
        try {
            boolean empty = runningConsumers == 0 && pendingStates == 0;
            LOG.debug(
                    "isEmpty={} runningConsumers={} pendingStates={} statesWithCandidates={}",
                    empty,
                    runningConsumers,
                    pendingStates,
                    statesWithCandidates);
            return empty;
        } finally {
            consumersReadLock.unlock();
        }
    }

    /**
     * @param fragmentManager
     * @return A new crawl task as soon as one is ready. Until then, it blocks.
     * @throws InterruptedException when taking from the queue is interrupted.
     */
    public StateVertex awaitNewTask(
            StateVertex currentState, List<StateVertex> onURLSet, FragmentManager fragmentManager)
            throws InterruptedException {
        if (currentState == null) {
            int id = consumeTask();
            return sfg.get().getById(id);
        }

        if (!(currentState instanceof HybridStateVertexImpl)) {
            return awaitNewTask();
        }

        StateVertex next = null;
        try {
            next = fragmentManager.getClosestUnexploredState(
                    currentState, onURLSet, statesWithCandidates, applyNonSelAdvantage);
        } catch (Exception ignored) {

        }

        if (next == null) {
            LOG.warn("Prioritization failed. So continuiing with FIFO QUeue");
            if (fragmentManager.getAllFragments() == null) {
                return null;
            }
            int id = consumeTask();
            return sfg.get().getById(id);
        }

        LOG.info("Next Best Task : " + next.getName());
        if (!next.hasUnexploredActions()) {
            LOG.info("Best Task is already explored. " + next.getName());
            unexploredStates = false;
        } else {
            unexploredStates = true;
        }
        return next;
    }

    public StateVertex awaitNewTask() throws InterruptedException {
        int id = consumeTask();
        LOG.debug("New task polled for state {}", id);
        return sfg.get().getById(id);
    }

    private int consumeTask() throws InterruptedException {
        int id = statesWithCandidates.take();

        consumersWriteLock.lock();
        try {
            runningConsumers++;
            pendingStates--;

            LOG.debug(
                    "Took state {}, there are {} running consumers and {} pending states",
                    id,
                    runningConsumers,
                    pendingStates);
        } finally {
            consumersWriteLock.unlock();
        }

        return id;
    }

    /**
     * Indicates that a task is done.
     *
     * <p> Should be called after processing a task.
     *
     * @param state the state of the task done.
     * @see #awaitNewTask()
     */
    void taskDone(StateVertex state) {
        if (state == null) {
            return;
        }

        consumersWriteLock.lock();
        try {
            runningConsumers--;

            int stateId = state.getId();
            Lock lock = locks.get(stateId);
            try {
                lock.lock();
                List<CandidateCrawlAction> queue = cache.get(stateId);
                if (queue != null && !queue.isEmpty()) {
                    addPendingState(state);
                }
            } finally {
                lock.unlock();
            }

            LOG.debug(
                    "Task done={} runningConsumers={} pendingStates={} statesWithCandidates={}",
                    stateId,
                    runningConsumers,
                    pendingStates,
                    statesWithCandidates);
        } finally {
            consumersWriteLock.unlock();
        }
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

        if (nextUnique == null) {
            return null;
        }

        while (true) {
            try {
                int id = consumeTask();
                if (id == nextUniqueId) {
                    break;
                }
            } catch (InterruptedException e) {
                LOG.error("Interruped while finding next unique state");
                LOG.debug(e.getMessage());
            }
        }

        return nextUnique;
    }

    public void purgeActionsForState(StateVertex crawlTask) {
        Lock lock = locks.get(crawlTask.getId());
        try {
            lock.lock();
            LOG.debug("Removing tasks for target state {}", crawlTask.getName());
            removeStateFromQueue(crawlTask.getId());
            List<CandidateCrawlAction> removed = cache.remove(crawlTask.getId());
            if (removed != null) {
                unfiredActionsCount.inc(removed.size());
                LOG.info("Placing purged actions in unreachable cache for {}", crawlTask.getName());
                unreachableCache.put(crawlTask.getId(), removed);
            }
        } finally {
            lock.unlock();
            crawlerLostCount.inc();
        }
    }

    public boolean disableInputsForAction(CandidateCrawlAction action) {
        if (!this.skipInputs.contains(action)) {
            this.skipInputs.add(action);
            return true;
        }
        return false;
    }

    public boolean shouldDisableInput(CandidateCrawlAction action) {
        return this.skipInputs.contains(action);
    }

    public boolean shouldDisableInput(Eventable event) {
        return this.skipInputsForPath.contains(event);
    }

    public void disableInputsForPath(Eventable event) {
        if (!this.skipInputsForPath.contains(event)) {
            LOG.info("Disabling related inputs for {} ", event.getId());
            LOG.info(
                    "event {} - Before {}",
                    event.getId(),
                    event.getRelatedFormInputs().size());
            event.setRelatedFormInputs(new ArrayList<>());
            LOG.info(
                    "event {} - After {}",
                    event.getId(),
                    event.getRelatedFormInputs().size());
            this.skipInputsForPath.add(event);
        }
    }

    public void mapInput(Eventable event, List<FormInput> worked) {
        LOG.info("Changing related inputs to worked inputs {} for {}", worked.size(), event.getId());
        LOG.info(
                "event {} - Before {}",
                event.getId(),
                event.getRelatedFormInputs().size());
        event.setRelatedFormInputs(worked);
        LOG.info(
                "event {} - After {}",
                event.getId(),
                event.getRelatedFormInputs().size());
        this.inputMap.put(event.getId(), worked);
        LOG.info(
                "input for {}, {}",
                event.getId(),
                this.inputMap.get(event.getId()).size());
    }

    public List<FormInput> getInput(Eventable event) {
        try {
            LOG.info("input map size {}", inputMap.size());
            return this.inputMap.get(event.getId());
        } catch (Exception ex) {
            return null;
        }
    }

    public void stateUpdated(StateVertex crawlTask) {
        LOG.info("Purging actions for updated {}", crawlTask.getName());
        purgeActionsForState(crawlTask);
    }

    public void removeAction(CandidateElement candidate, StateVertex state) {
        if (unreachableCache.get(state.getId()) != null) {
            rediscoveredState(state);
        }
        if (statesWithCandidates.contains(state.getId())) {
            List<CandidateCrawlAction> availableActions = cache.get(state.getId());
            CandidateCrawlAction toRemove = null;
            for (CandidateCrawlAction action : availableActions) {
                if (action.getCandidateElement().equals(candidate)) {
                    toRemove = action;
                    break;
                }
            }
            if (toRemove != null) {
                availableActions.remove(toRemove);
            }
            if (availableActions.isEmpty()) {
                LOG.debug("All actions polled for state {}", state.getName());
                cache.remove(state.getId());
                removeStateFromQueue(state.getId());
                LOG.debug("There are now {} states with unfinished actions", cache.size());
            }
        }
    }

    CandidateCrawlAction pollActionOrNull(StateVertex state) {
        LOG.debug("Polling action for state {}", state.getName());
        Lock lock = locks.get(state.getId());
        try {
            lock.lock();
            List<CandidateCrawlAction> queue = cache.get(state.getId());
            if (queue == null) {
                return null;
            } else {
                CandidateCrawlAction action = queue.remove(0);
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
}
