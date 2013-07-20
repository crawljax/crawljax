package com.crawljax.di;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawlSessionNotSetupYetException;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.state.InMemoryStateFlowGraph;
import com.crawljax.core.state.StateVertex;
import com.google.common.base.Preconditions;

/**
 * Takes care of the lazy initialization of the {@link CrawlSession}.
 */
@Singleton
public class CrawlSessionProvider implements Provider<CrawlSession> {

	private static final Logger LOG = LoggerFactory.getLogger(CrawlSessionProvider.class);

	private final AtomicBoolean isSet = new AtomicBoolean();
	private final InMemoryStateFlowGraph stateFlowGraph;
	private final CrawljaxConfiguration config;
	private final MetricRegistry registry;

	private CrawlSession session;

	@Inject
	public CrawlSessionProvider(InMemoryStateFlowGraph stateFlowGraph,
	        CrawljaxConfiguration config, MetricRegistry registry) {
		this.stateFlowGraph = stateFlowGraph;
		this.config = config;
		this.registry = registry;
	}

	/**
	 * @throws IllegalStateException
	 *             when the method is invoked more than once.
	 */
	public void setup(StateVertex indexState) {
		if (!isSet.getAndSet(true)) {
			LOG.debug("Setting up the crawlsession");
			StateVertex added = stateFlowGraph.putIndex(indexState);
			Preconditions.checkArgument(added == null, "Could not set the initial state");
			session = new CrawlSession(config, stateFlowGraph, indexState, registry);
		} else {
			throw new IllegalStateException("Session is already set");
		}
	}

	@Override
	public CrawlSession get() {
		if (isSet.get()) {
			return this.session;
		} else {
			throw new CrawlSessionNotSetupYetException();
		}
	}

}
