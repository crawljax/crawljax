package com.crawljax.core;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.codahale.metrics.MetricRegistry;
import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.browserwaiter.WaitConditionChecker;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.Plugins;
import com.crawljax.core.state.DefaultStateVertexFactory;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.core.state.InMemoryStateFlowGraph;
import com.crawljax.core.state.StateMachine;
import com.crawljax.core.state.StateVertex;
import com.crawljax.di.CoreModule.CandidateElementExtractorFactory;
import com.crawljax.di.CoreModule.FormHandlerFactory;
import com.crawljax.di.CoreModule.TrainingFormHandlerFactory;
import com.crawljax.forms.FormHandler;
import com.crawljax.forms.FormInput;
import com.crawljax.forms.TrainingFormHandler;
import com.crawljax.oraclecomparator.StateComparator;
import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.util.ArrayList;
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
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test class for the Crawler testing.
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class CrawlerTest {

    private URI url;

    private String strippedDom;

    private Crawler crawler;

    private StateMachine sm;

    @Mock
    private EmbeddedBrowser browser;

    @Spy
    private Plugins plugins =
            new Plugins(CrawljaxConfiguration.builderFor("http://localhost").build(), new MetricRegistry());

    @Mock
    private Provider<CrawlSession> sessionProvider;

    @Mock
    private CrawlSession session;

    private StateComparator stateComparator;

    @Mock
    private FormHandler formHandler;

    @Mock
    private TrainingFormHandler trainingFormHandler;

    @Mock
    private WaitConditionChecker waitConditionChecker;

    @Mock
    private CandidateElementExtractor extractor;

    //	@Mock
    //	private UnfiredCandidateActions candidateActionCache;

    @Mock
    private UnfiredFragmentCandidates candidateActionCache;

    @Mock
    private StateVertex index;

    @Mock
    private StateVertex target;

    @Mock
    private InMemoryStateFlowGraph graph;

    @Mock
    private Provider<InMemoryStateFlowGraph> graphProvider;

    @Mock
    private Eventable eventToTransferToTarget;

    @Captor
    private ArgumentCaptor<List<FormInput>> formInputsCaptor;

    @Mock
    private CandidateElement action;

    @Mock
    private ExitNotifier exitNotifier;

    private CrawlerContext context;

    @Before
    public void setup() {
        CandidateElementExtractorFactory elementExtractor = mock(CandidateElementExtractorFactory.class);
        when(elementExtractor.newExtractor(browser)).thenReturn(extractor);
        FormHandlerFactory formHandlerFactory = mock(FormHandlerFactory.class);
        when(formHandlerFactory.newFormHandler(browser)).thenReturn(formHandler);
        TrainingFormHandlerFactory trainingFormHandlerFactory = mock(TrainingFormHandlerFactory.class);
        when(trainingFormHandlerFactory.newTrainingFormHandler(browser)).thenReturn(trainingFormHandler);
        url = URI.create("http://example.com");

        strippedDom = "<html><body><p>Hello</p></body></html>";

        when(browser.getCurrentUrl()).thenReturn(url.toString());
        when(browser.getStrippedDom()).thenReturn(strippedDom);
        when(sessionProvider.get()).thenReturn(session);

        CrawljaxConfiguration config =
                Mockito.spy(CrawljaxConfiguration.builderFor(url).build());
        stateComparator = new StateComparator(config.getCrawlRules());

        when(extractor.extract(target)).thenReturn(ImmutableList.of(action));
        when(graphProvider.get()).thenReturn(graph);

        context = new CrawlerContext(browser, config, sessionProvider, exitNotifier, new MetricRegistry());
        crawler = new Crawler(
                context,
                config,
                stateComparator,
                candidateActionCache,
                formHandlerFactory,
                null,
                waitConditionChecker,
                elementExtractor,
                graphProvider,
                plugins,
                new DefaultStateVertexFactory());

        when(candidateActionCache.pollActionOrNull(index)).thenReturn(null);
        when(candidateActionCache.getInput(Mockito.any())).thenReturn(null);
        setupStateFlowGraph();
        setStateMachineForConfig(config);
        crawler.setStateMachine(sm);
    }

    private void setupStateFlowGraph() {
        when(index.getId()).thenReturn(1);
        when(index.getName()).thenReturn("Index");
        when(index.toString()).thenReturn("index");
        when(target.getId()).thenReturn(2);
        when(target.getName()).thenReturn("State 2");
        when(target.toString()).thenReturn("target");

        when(eventToTransferToTarget.getIdentification()).thenReturn(new Identification(How.name, "//DIV[@id='click]"));
        when(eventToTransferToTarget.getRelatedFrame()).thenReturn("");
        when(eventToTransferToTarget.getSourceStateVertex()).thenReturn(index);
        when(eventToTransferToTarget.getTargetStateVertex()).thenReturn(target);
        when(eventToTransferToTarget.getRelatedFormInputs())
                .thenReturn(ImmutableList.copyOf(new CopyOnWriteArrayList<>()));
        when(graph.getShortestPath(index, target)).thenReturn(ImmutableList.of(eventToTransferToTarget));
        when(graph.getInitialState()).thenReturn(index);
        when(session.getStateFlowGraph()).thenReturn(graph);
        when(session.getInitialState()).thenReturn(index);

        when(graph.canGoTo(index, target)).thenReturn(true);
        when(graph.putIfAbsent(index)).thenReturn(index);

        when(graph.newStateFor(Mockito.any(), anyString(), anyString(), Mockito.any()))
                .thenReturn(index, index, index, index, target);
    }

    private void setStateMachineForConfig(CrawljaxConfiguration config) {
        List<StateVertex> onURLSet = new ArrayList<>();
        onURLSet.add(index);
        sm = new StateMachine(graph, config.getCrawlRules().getInvariants(), plugins, null, onURLSet);
    }

    @Test
    public void whenResetTheStateIsBackToIndex() {
        crawler.reset(0);
        verifyCrawlerReset(inOrder(plugins, browser));
    }

    private void verifyCrawlerReset(InOrder order) {
        order.verify(browser).goToUrl(url);
        order.verify(plugins).runOnUrlLoadPlugins(context);
    }

    @Test
    public void whenExecuteTaskTheCrawlIsCompletedCorrectly() throws Exception {
        when(extractor.checkCrawlCondition()).thenReturn(true);
        when(browser.fireEventAndWait(eventToTransferToTarget)).thenReturn(true);

        crawler.execute(target);
        InOrder order = inOrder(extractor, browser, formHandler, plugins, waitConditionChecker, candidateActionCache);
        //		InOrder order = inOrder(extractor, plugins, browser, formHandler, waitConditionChecker);
        //				, formHandler, plugins, waitConditionChecker,candidateActionCache);
        verifyPathIsFollowed(order);
    }

    private void verifyPathIsFollowed(InOrder order) {
        verifyCrawlerReset(order);
        order.verify(extractor).checkCrawlCondition();
        verifyFormElementsChecked(order);
        order.verify(waitConditionChecker).wait(browser);
        order.verify(browser).closeOtherWindows();
        order.verify(plugins).runOnRevisitStatePlugins(context, target);
        order.verify(extractor).checkCrawlCondition();
        order.verify(candidateActionCache).pollActionOrNull(target);
    }

    private void verifyFormElementsChecked(InOrder order) {
        order.verify(formHandler).getFormInputs();
        order.verify(formHandler).handleFormElements(formInputsCaptor.capture());
        formInputsCaptor.getValue().isEmpty();
    }
}
