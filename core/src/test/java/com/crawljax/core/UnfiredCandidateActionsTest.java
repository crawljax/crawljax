package com.crawljax.core;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertex;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@RunWith(MockitoJUnitRunner.class)
public class UnfiredCandidateActionsTest {

	private static final int CONSUMERS = 4;
	private static final int EVENTS_PER_CONSUMER = 20;

	private static class TaskConsumer implements Callable<List<Eventable>> {

		private static final Logger LOG =
		        LoggerFactory.getLogger(UnfiredCandidateActionsTest.TaskConsumer.class);
		private final UnfiredCandidateActions actions;
		private final List<Eventable> eventables = Lists.newLinkedList();
		private final StateVertex contributeTo;
		private int createEvents = EVENTS_PER_CONSUMER;

		TaskConsumer(UnfiredCandidateActions actions, StateVertex contributeTo) {
			this.actions = actions;
			this.contributeTo = contributeTo;
		}

		@Override
		public List<Eventable> call() throws Exception {
			while (!Thread.interrupted()) {
				try {
					LOG.debug("Wait for task");
					CrawlTask task = actions.awaitNewTask();
					LOG.debug("Got new task");
					eventables.addAll(task.getEventables());
					if (createEvents > 0) {
						createEvents--;
						spawnEvent();
					}
				} catch (InterruptedException e) {
					return eventables;
				}
			}
			return eventables;
		}

		private void spawnEvent() {
			ImmutableList.Builder<CandidateCrawlAction> newActions = ImmutableList.builder();
			for (int i = 0; i < 3; i++) {
				CandidateCrawlAction action = mock(CandidateCrawlAction.class);
				newActions.add(action);
			}
			actions.addActions(newActions.build(), contributeTo);
		}
	}

	@Mock
	private StateFlowGraph graph;

	@Mock
	private Provider<StateFlowGraph> graphProvider;

	private Map<Integer, StateVertex> states;

	private UnfiredCandidateActions candidateActions;

	private ExecutorService executor;

	@Before
	public void setup() {
		when(graphProvider.get()).thenReturn(graph);
		BrowserConfiguration config = new BrowserConfiguration(BrowserType.firefox);
		candidateActions = new UnfiredCandidateActions(config, graphProvider);
		executor = Executors.newFixedThreadPool(CONSUMERS);

		states = Maps.newHashMap();
	}

	@Test
	public void testAddingAndTakingFromTheQueue() throws InterruptedException {
		for (int i = 0; i < CONSUMERS; i++) {
			StateVertex state = mock(StateVertex.class);
			when(state.getId()).thenReturn(i + 1);
			states.put(i + 1, state);
			executor.submit(new TaskConsumer(candidateActions, state));
		}
		candidateActions.addActions(ImmutableList.<CandidateCrawlAction> of(), states.get(1));
		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.MINUTES);

	}

	CrawlTask newMockCrawlTask(int size) {
		ImmutableList.Builder<Eventable> eventableBuilder = ImmutableList.builder();
		for (int i = 0; i < size; i++) {
			Eventable event = Mockito.mock(Eventable.class);
			eventableBuilder.add(event);
		}
		return new CrawlTask(eventableBuilder.build());
	}

}
