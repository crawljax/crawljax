package com.crawljax.plugins.crawloverview.model;

import javax.annotation.concurrent.Immutable;

import com.crawljax.core.configuration.CrawljaxConfiguration;
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
	private final CrawljaxConfiguration configuration;

	public OutPutModel(ImmutableMap<String, State> states, ImmutableSet<Edge> edges,
	        Statistics statistics, CrawljaxConfiguration config) {
		this.states = states;
		this.edges = edges;
		this.statistics = statistics;
		configuration = config;
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

	public CrawljaxConfiguration getConfiguration() {
		return configuration;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OutPutModel [states=");
		builder.append(states);
		builder.append(", edges=");
		builder.append(edges);
		builder.append(", statistics=");
		builder.append(statistics);
		builder.append(", configuration=");
		builder.append(configuration);
		builder.append("]");
		return builder.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((configuration == null) ? 0 : configuration.hashCode());
		result = prime * result + ((edges == null) ? 0 : edges.hashCode());
		result = prime * result + ((states == null) ? 0 : states.hashCode());
		result = prime * result + ((statistics == null) ? 0 : statistics.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		OutPutModel other = (OutPutModel) obj;
		if (configuration == null) {
			if (other.configuration != null) {
				return false;
			}
		} else if (!configuration.equals(other.configuration)) {
			return false;
		}
		if (edges == null) {
			if (other.edges != null) {
				return false;
			}
		} else if (!edges.equals(other.edges)) {
			return false;
		}
		if (states == null) {
			if (other.states != null) {
				return false;
			}
		} else if (!states.equals(other.states)) {
			return false;
		}
		if (statistics == null) {
			if (other.statistics != null) {
				return false;
			}
		} else if (!statistics.equals(other.statistics)) {
			return false;
		}
		return true;
	}

}
