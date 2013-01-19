package com.crawljax.plugins.crawloverview.model;

import javax.annotation.concurrent.Immutable;

import com.crawljax.core.configuration.CrawlSpecificationReader;
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
	private final CrawlConfiguration configuration;
	private final CrawlSpecificationReader crawlSpecification;

	public OutPutModel(ImmutableMap<String, State> states, ImmutableSet<Edge> edges,
	        Statistics statistics, CrawlConfiguration config, CrawlSpecificationReader spec) {
		this.states = states;
		this.edges = edges;
		this.statistics = statistics;
		configuration = config;
		this.crawlSpecification = spec;
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

	public CrawlConfiguration getConfiguration() {
		return configuration;
	}

	public CrawlSpecificationReader getCrawlSpecification() {
		return crawlSpecification;
	}
}
