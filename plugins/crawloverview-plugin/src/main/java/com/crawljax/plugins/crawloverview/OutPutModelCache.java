package com.crawljax.plugins.crawloverview;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.concurrent.ThreadSafe;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateVertex;
import com.crawljax.plugins.crawloverview.model.CandidateElementPosition;
import com.crawljax.plugins.crawloverview.model.CrawlConfiguration;
import com.crawljax.plugins.crawloverview.model.Edge;
import com.crawljax.plugins.crawloverview.model.OutPutModel;
import com.crawljax.plugins.crawloverview.model.State;
import com.crawljax.plugins.crawloverview.model.StateStatistics;
import com.crawljax.plugins.crawloverview.model.Statistics;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

/**
 * Cache to build the {@link OutPutModel}. It is {@link ThreadSafe} except for the
 * {@link CandidateElementPosition} class.
 */
class OutPutModelCache {

	private final ConcurrentMap<String, StateBuilder> states = Maps.newConcurrentMap();
	private final BlockingDeque<Edge> edges = Queues.newLinkedBlockingDeque();

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
	OutPutModel close(CrawlSession session) {
		HashSet<Edge> buffer = new HashSet<Edge>(edges.size());
		edges.drainTo(buffer);
		ImmutableSet<Edge> edgesCopy = ImmutableSet.copyOf(buffer);
		checkEdgesAndCountFans(edgesCopy);
		ImmutableMap<String, State> statesCopy = buildStates();
		StateStatistics stateStats = new StateStatistics(statesCopy.values());
		return new OutPutModel(statesCopy, edgesCopy,
		        new Statistics(session, stateStats),
		        new CrawlConfiguration(session),
		        session.getCrawljaxConfiguration().getCrawlSpecificationReader());
	}

	private void checkEdgesAndCountFans(ImmutableSet<Edge> edges) {
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

	public void addEdges(Set<Eventable> allEdges) {
		for (Eventable eventable : allEdges) {
			edges.add(new Edge(eventable));
		}
	}
}
