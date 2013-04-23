package com.crawljax.core;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.browserwaiter.WaitConditionChecker;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.Plugins;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertex;
import com.crawljax.di.CoreModule.CandidateElementExtractorFactory;
import com.crawljax.di.CoreModule.FormHandlerFactory;
import com.crawljax.forms.FormHandler;
import com.crawljax.forms.FormInput;
import com.crawljax.oraclecomparator.StateComparator;
import com.google.common.collect.ImmutableList;

/**
 * Test class for the Crawler testing.
 */
@RunWith(MockitoJUnitRunner.class)
public class CrawlerTest {

	private URL url;

	private Crawler crawler;

	@Mock
	private EmbeddedBrowser browser;

	@Spy
	private Plugins plugins = Plugins.noPlugins();

	@Mock
	private Provider<CrawlSession> sessionProvider;

	@Mock
	private CrawlSession session;

	private StateComparator stateComparator;

	@Mock
	private FormHandler formHandler;

	@Mock
	private WaitConditionChecker waitConditionChecker;

	@Mock
	private CandidateElementExtractor extractor;

	@Mock
	private UnfiredCandidateActions candidateActionCache;

	@Mock
	private StateVertex index;

	@Mock
	private StateVertex target;

	@Mock
	private StateFlowGraph graph;

	@Mock
	private Eventable eventToTransferToTarget;

	@Captor
	private ArgumentCaptor<List<FormInput>> formInputsCaptor;

	@Mock
	private CandidateElement action;

	@Before
	public void setup() throws MalformedURLException {
		CandidateElementExtractorFactory elementExtractor =
		        mock(CandidateElementExtractorFactory.class);
		when(elementExtractor.newExtractor(browser)).thenReturn(extractor);
		FormHandlerFactory formHandlerFactory = mock(FormHandlerFactory.class);
		when(formHandlerFactory.newFormHandler(browser)).thenReturn(formHandler);
		url = new URL("http://example.com");
		when(sessionProvider.get()).thenReturn(session);

		CrawljaxConfiguration config = CrawljaxConfiguration.builderFor(url).build();
		stateComparator = new StateComparator(config.getCrawlRules());

		when(extractor.extract(target)).thenReturn(ImmutableList.of(action));

		crawler =
		        new Crawler(browser, config, sessionProvider,
		                stateComparator,
		                candidateActionCache, formHandlerFactory, waitConditionChecker,
		                elementExtractor);

		setupStateFlowGraph();
	}

	private void setupStateFlowGraph() {
		when(index.getId()).thenReturn(1);
		when(index.getName()).thenReturn("Index");
		when(target.getId()).thenReturn(2);
		when(target.getName()).thenReturn("State 2");

		when(eventToTransferToTarget.getIdentification()).thenReturn(
		        new Identification(How.name, "//DIV[@id='click]"));
		when(eventToTransferToTarget.getRelatedFrame()).thenReturn("");
		when(eventToTransferToTarget.getSourceStateVertex()).thenReturn(index);
		when(eventToTransferToTarget.getTargetStateVertex()).thenReturn(target);
		when(eventToTransferToTarget.getRelatedFormInputs()).thenReturn(
		        new CopyOnWriteArrayList<FormInput>());
		when(graph.getShortestPath(index, target)).thenReturn(
		        ImmutableList.of(eventToTransferToTarget));
		when(graph.getInitialState()).thenReturn(index);
		when(session.getStateFlowGraph()).thenReturn(graph);
		when(session.getInitialState()).thenReturn(index);

		when(graph.canGoTo(index, target)).thenReturn(true);
	}

	@Test
	public void whenResetTheStateIsBackToIndex() {
		crawler.reset();
		verifyCrawlerReset(inOrder(plugins, browser));
	}

	private void verifyCrawlerReset(InOrder order) {
		order.verify(browser).goToUrl(url);
		order.verify(plugins).runOnUrlLoadPlugins(browser);
	}

	@Test
	public void whenExecuteTaskTheCrawlisCompletedCorrectly() {
		when(extractor.checkCrawlCondition()).thenReturn(true);
		when(browser.fireEventAndWait(eventToTransferToTarget)).thenReturn(true);

		crawler.execute(target);
		InOrder order =
		        inOrder(extractor, browser, formHandler, plugins, waitConditionChecker,
		                candidateActionCache);
		verifyPathIsFollowed(order);
	}

	private void verifyPathIsFollowed(InOrder order) {
		verifyCrawlerReset(order);
		order.verify(extractor).checkCrawlCondition();
		verifyFormElementsChecked(order);
		order.verify(waitConditionChecker).wait(browser);
		order.verify(browser).closeOtherWindows();
		order.verify(plugins).runOnRevisitStatePlugins(session, target);
		order.verify(extractor).checkCrawlCondition();
		order.verify(candidateActionCache).pollActionOrNull(target);
	}

	private void verifyFormElementsChecked(InOrder order) {
		order.verify(formHandler).getFormInputs();
		order.verify(formHandler).handleFormElements(formInputsCaptor.capture());
		formInputsCaptor.getValue().isEmpty();
	}
}
