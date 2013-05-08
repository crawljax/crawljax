package com.crawljax.plugins.crawloverview;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.ExitNotifier.ExitStatus;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateVertex;
import com.crawljax.plugins.crawloverview.model.CandidateElementPosition;
import com.crawljax.plugins.crawloverview.model.Edge;
import com.crawljax.plugins.crawloverview.model.OutPutModel;
import com.crawljax.plugins.crawloverview.model.State;
import com.crawljax.plugins.crawloverview.model.StateStatistics;
import com.crawljax.plugins.crawloverview.model.Statistics;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Maps;

/**
 * Cache to build the {@link OutPutModel}. It is {@link ThreadSafe} except for the
 * {@link CandidateElementPosition} class.
 */
class OutPutModelCache {

	private static final Logger LOG = LoggerFactory
	        .getLogger(OutPutModelCache.class);
	private final ConcurrentMap<String, StateBuilder> states = Maps
	        .newConcurrentMap();

	private final AtomicInteger failedEvents = new AtomicInteger();

	private final Date startDate = new Date();

	StateBuilder addStateIfAbsent(StateVertex state) {
		StateBuilder newState = new StateBuilder(state);
		StateBuilder found = states.putIfAbsent(state.getName(), newState);
		if (found == null) {
			return newState;
		} else {
			return found;
		}
	}

	/**
	 * @return Makes the final calculations and retuns the {@link OutPutModel}.
	 */
	public OutPutModel close(CrawlSession session, ExitStatus exitStatus) {
		ImmutableList<Edge> edgesCopy = asEdges(session.getStateFlowGraph()
		        .getAllEdges());
		checkEdgesAndCountFans(edgesCopy);
		ImmutableMap<String, State> statesCopy = buildStates();

		if (statesCopy.size() != session.getStateFlowGraph().getAllStates()
		        .size()) {
			LOG.error("Not all states from the session are in the result. This means there's a bug somewhere");
			LOG.info(
			        "Printing state difference. \nSession states: {} \nResult states: {}",
			        statesCopy, session.getStateFlowGraph().getAllStates());
		}

		StateStatistics stateStats = new StateStatistics(statesCopy.values());
		return new OutPutModel(statesCopy, edgesCopy, new Statistics(session,
		        stateStats, startDate, failedEvents.get()), exitStatus);
	}

	private ImmutableList<Edge> asEdges(Set<Eventable> allEdges) {
		ImmutableList.Builder<Edge> builder = ImmutableList.builder();
		for (Eventable eventable : allEdges) {
			builder.add(new Edge(eventable));
		}
		return builder.build();
	}

	private void checkEdgesAndCountFans(ImmutableList<Edge> edges) {
		for (Edge e : edges) {
			StateBuilder from = states.get(e.getFrom());
			StateBuilder to = states.get(e.getTo());
			checkNotNull(from, "From state %s is unkown", e.getFrom());
			checkNotNull(to, "To state %s is unkown", e.getTo());
			from.incrementFanOut();
			to.incrementFanIn();
		}
	}

	private ImmutableMap<String, State> buildStates() {
		Builder<String, State> builder = ImmutableMap.builder();
		for (StateBuilder state : states.values()) {
			builder.put(state.getName(), state.build());
		}
		return builder.build();
	}

	public void registerFailEvent(StateVertex currentState, Eventable eventable) {
		failedEvents.incrementAndGet();
		if (currentState != null) {
			StateBuilder builder = states.get(currentState.getName());
			if (builder != null) {
				builder.eventFailed(eventable);
			}
		}
	}

}
