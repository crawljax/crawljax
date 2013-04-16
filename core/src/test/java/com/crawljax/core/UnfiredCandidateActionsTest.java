package com.crawljax.core;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertex;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

@RunWith(MockitoJUnitRunner.class)
public class UnfiredCandidateActionsTest {

	private static final int CONSUMERS = 4;
	private static final int EVENTS_PER_CONSUMER = 20;

	@Mock
	private StateFlowGraph graph;

	@Mock
	private Provider<StateFlowGraph> graphProvider;

	private Map<Integer, StateVertex> states;

	private UnfiredCandidateActions candidateActions;

	private ExecutorService executor;
	private AtomicInteger runningConsumers;
	private CountDownLatch consumersDoneLatch;

	@Mock
	private NewCrawler crawler;

	@Before
	public void setup() {
		when(graphProvider.get()).thenReturn(graph);
		// todo deze niet null terug geven.
		when(graph.getShortestPath(any(StateVertex.class), any(StateVertex.class))).thenReturn(
		        ImmutableList.<Eventable> of());
		BrowserConfiguration config = new BrowserConfiguration(BrowserType.firefox);
		candidateActions = new UnfiredCandidateActions(config, graphProvider);
		executor = Executors.newFixedThreadPool(CONSUMERS);

		states = Maps.newHashMap();

		runningConsumers = new AtomicInteger();
		consumersDoneLatch = new CountDownLatch(1);

	}

	@Test(timeout = 2000L)
	public void testAddingAndTakingFromTheQueue() throws InterruptedException, ExecutionException {
		List<Future<Void>> doneEventables = new ArrayList<>(CONSUMERS);
		candidateActions.addActions(ImmutableList.<CandidateCrawlAction> of(), states.get(1));
		for (int i = 0; i < CONSUMERS; i++) {
			StateVertex state = mock(StateVertex.class);
			when(state.getId()).thenReturn(i + 1);
			states.put(i + 1, state);
			doneEventables.add(executor.submit(new CrawlTaskConsumer(candidateActions,
			        runningConsumers, consumersDoneLatch, crawler)));
		}
		for (Future<Void> future : doneEventables) {
			future.get();
		}
	}

}
