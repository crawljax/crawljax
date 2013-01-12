package com.crawljax.plugins.crawloverview.model;

import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Result of a Crawl session.
 * <p>
 * This class is nearly {@link Immutable}. Unfortunately {@link State#getCandidateElements()} isn't
 * so that might leak state.
 */
@Immutable
public final class OutPutModel {

	private final ImmutableMap<String, State> states;
	private final ImmutableSet<Edge> edges;
	private final Statistics statistics;

	public OutPutModel(ImmutableMap<String, State> states, ImmutableSet<Edge> edges,
	        Statistics statistics) {
		this.states = states;
		this.edges = edges;
		this.statistics = statistics;
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

	public ImmutableMap<String, State> getStates() {
		return states;
	}

	public ImmutableSet<Edge> getEdges() {
		return edges;
	}

	public Statistics getStatistics() {
		return statistics;
	}
}
