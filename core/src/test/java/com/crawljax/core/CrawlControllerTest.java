package com.crawljax.core;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Provider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.codahale.metrics.MetricRegistry;
import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.ExitNotifier.ExitStatus;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.Plugins;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.state.InMemoryStateFlowGraph;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertex;
import com.crawljax.di.CrawlSessionProvider;

@RunWith(MockitoJUnitRunner.class)
public class CrawlControllerTest {

	@Mock
	private InMemoryStateFlowGraph graph;

	@Mock
	private Provider<StateFlowGraph> graphProvider;

	private UnfiredCandidateActions candidateActions;

	private ExecutorService executor;
	private ExitNotifier consumersDoneLatch;

	@Mock
	private Crawler crawler;

	@Mock
	private Provider<CrawlTaskConsumer> consumerFactory;

	private CrawlSessionProvider crawlSessionProvider;

	private CrawlController controller;

	@Mock
	private StateVertex index;

	@Mock
	private StateVertex state3;

	@Mock
	private StateVertex state2;

	private AtomicInteger polledActions;

	@Mock
	private PostCrawlingPlugin postCrawlPlugin;

	@Before
	public void setup() {
		setupGraphAndStates();

		polledActions = new AtomicInteger();
		Mockito.doAnswer(new Answer<Void>() {

			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				StateVertex task = (StateVertex) invocation.getArguments()[0];
				CandidateCrawlAction action = candidateActions.pollActionOrNull(task);
				while (action != null) {
					// Slight delay to simulate the threading better
					Thread.sleep(25);
					polledActions.incrementAndGet();
					action = candidateActions.pollActionOrNull(task);
				}
				return null;
			}
		}).when(crawler).execute(any(StateVertex.class));
	}

	private void setupGraphAndStates() {
		when(graphProvider.get()).thenReturn(graph);

		when(index.getId()).thenReturn(1);
		when(graph.getById(1)).thenReturn(index);
		when(crawler.crawlIndex()).thenReturn(index);

		when(state2.getId()).thenReturn(2);
		when(state3.getId()).thenReturn(3);
		when(graph.getById(2)).thenReturn(state2);
		when(graph.getById(3)).thenReturn(state3);
		setNames(index, state2, state3);
	}

	private void setNames(StateVertex... state) {
		for (StateVertex stateVertex : state) {
			String name = "State-" + stateVertex.getId();
			when(stateVertex.getName()).thenReturn(name);
		}

	}

	private void setupForConsumers(int consumers) {
		executor = Executors.newFixedThreadPool(consumers + 2);
		CrawljaxConfiguration config =
		        CrawljaxConfiguration
		                .builderFor("http://example.com")
		                .addPlugin(postCrawlPlugin)
		                .setBrowserConfig(
		                        new BrowserConfiguration(BrowserType.FIREFOX, consumers))
		                .build();

		candidateActions =
		        new UnfiredCandidateActions(config.getBrowserConfig(), graphProvider,
		                new MetricRegistry());

		consumersDoneLatch = new ExitNotifier(config.getMaximumStates());

		when(consumerFactory.get()).thenReturn(new CrawlTaskConsumer(candidateActions,
		        consumersDoneLatch, crawler));

		crawlSessionProvider = new CrawlSessionProvider(graph, config, new MetricRegistry());

		Plugins plugins = new Plugins(config, new MetricRegistry());
		controller = new CrawlController(executor, consumerFactory, config, consumersDoneLatch,
		        crawlSessionProvider, plugins);

	}

	@Test(timeout = 5000L)
	public void withASingleTaskTheCrawlerTerminates() {
		setupForConsumers(1);
		runWithOneTask();
	}

	@Test(timeout = 5000L)
	public void withASingleTaskMultipleConsumersTheCrawlerTerminates() {
		setupForConsumers(4);
		runWithOneTask();
	}

	private void runWithOneTask() {
		candidateActions.addActions(mockActions(1), index);
		controller.call();
		assertThat(polledActions.get(), is(1));
	}

	@Test(timeout = 5000L)
	public void withSixTasksTheCrawlerTerminates() {
		setupForConsumers(1);
		candidateActions.addActions(mockActions(2), index);
		candidateActions.addActions(mockActions(2), state2);
		candidateActions.addActions(mockActions(2), state3);
		controller.call();
		assertThat(polledActions.get(), is(6));
	}

	@Test(timeout = 50_000)
	public void withManyActionsMultipleConsumersTheCrawlerTerminates() {
		setupForConsumers(4);
		runWith300Actions();
		verify(crawler, times(4)).close();
	}

	private void runWith300Actions() {
		candidateActions.addActions(mockActions(200), index);
		candidateActions.addActions(mockActions(200), state2);
		candidateActions.addActions(mockActions(200), state3);
		controller.call();
		assertThat(polledActions.get(), is(600));

	}

	public List<CandidateCrawlAction> mockActions(int size) {
		List<CandidateCrawlAction> list = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			CandidateCrawlAction action = mock(CandidateCrawlAction.class);
			list.add(action);
		}
		return list;
	}

	@After
	public void verifyPerfectEndState() {
		assertThat(candidateActions.isEmpty(), is(true));
		assertThat(consumersDoneLatch.isExitCalled(), is(true));
		verify(postCrawlPlugin).postCrawling(crawlSessionProvider.get(), ExitStatus.EXHAUSTED);
	}
}
