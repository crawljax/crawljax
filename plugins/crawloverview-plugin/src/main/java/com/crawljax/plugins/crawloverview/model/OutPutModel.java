package com.crawljax.plugins.crawloverview.model;

import javax.annotation.concurrent.Immutable;

import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.base.Objects;

/**
 * Result of a Crawl session.
 * <p>
 * This class is nearly {@link Immutable}. Unfortunately {@link State#getCandidateElements()} isn't
 * so that might leak state.
 */
@Immutable
public final class OutPutModel {

	private final ImmutableMap<String, State> states;
	private final ImmutableList<Edge> edges;
	private final Statistics statistics;
	private final CrawljaxConfiguration configuration;

	public OutPutModel(ImmutableMap<String, State> states, ImmutableList<Edge> edges,
	        Statistics statistics, CrawljaxConfiguration config) {
		this.states = states;
		this.edges = edges;
		this.statistics = statistics;
		configuration = config;
	}

	public ImmutableMap<String, State> getStates() {
		return states;
	}

	public ImmutableList<Edge> getEdges() {
		return edges;
	}

	public Statistics getStatistics() {
		return statistics;
	}

	public CrawljaxConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(states, edges, statistics, configuration);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof OutPutModel) {
			OutPutModel that = (OutPutModel) object;
			return Objects.equal(this.states, that.states)
			        && Objects.equal(this.edges, that.edges)
			        && Objects.equal(this.statistics, that.statistics)
			        && Objects.equal(this.configuration, that.configuration);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
		        .add("states", states)
		        .add("edges", edges)
		        .add("statistics", statistics)
		        .add("configuration", configuration)
		        .toString();
	}

}
