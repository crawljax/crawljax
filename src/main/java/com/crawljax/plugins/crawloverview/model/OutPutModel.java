package com.crawljax.plugins.crawloverview.model;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateVertex;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

public class OutPutModel {

	private final Map<String, State> states = Maps.newLinkedHashMap();
	private final Set<Edge> edges = new LinkedHashSet<Edge>();

	public State addStateIfAbsent(StateVertex state) {
		if (states.containsKey(state.getName())) {
			return states.get(state.getName());
		} else {
			State newState = new State(state);
			states.put(state.getName(), newState);
			return newState;
		}
	}

	public void addEdge(Edge edge) {
		edges.add(edge);
	}

	public void checkForConsistency() {
		checkAllReferencedStatesExist();
	}

	public Collection<State> getStates() {
		return states.values();
	}

	private void checkAllReferencedStatesExist() {
		for (Edge e : edges) {
			checkArgument(states.containsKey(e.getFrom()), "From state %s is unkown", e.getFrom());
			checkArgument(states.containsKey(e.getTo()), "To state %s is unkown", e.getTo());
		}
	}

	public String toJson() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public void setEdges(Set<Eventable> allEdges) {
		for (Eventable eventable : allEdges) {
			edges.add(new Edge(eventable));
		}
	}
}
