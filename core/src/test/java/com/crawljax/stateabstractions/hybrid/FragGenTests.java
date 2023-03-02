package com.crawljax.stateabstractions.hybrid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.eventablecondition.EventableConditionChecker;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CandidateElementExtractor;
import com.crawljax.core.ExitNotifier;
import com.crawljax.core.ExtractorManager;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.state.InMemoryStateFlowGraph;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StatePair.StateComparision;
import com.crawljax.core.state.StateVertex;
import com.crawljax.forms.FormHandler;
import com.crawljax.fragmentation.FragmentManager;
import com.crawljax.fragmentation.FragmentationPlugin;
import com.crawljax.util.DomUtils;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.inject.Provider;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

@RunWith(MockitoJUnitRunner.class)
public class FragGenTests {

    @Mock
    private Provider<InMemoryStateFlowGraph> graphProvider;

    @Mock
    private Provider<StateFlowGraph> sfgProvider;

    @Mock
    private EmbeddedBrowser browser;

    @Mock
    private ExtractorManager checker;

    @Mock
    private FormHandler handler;

    private InMemoryStateFlowGraph sfg;

    private Object LOG;

    private EventableConditionChecker eventableChecker;

    @Test
    public void testTreeDiffSimpleDOM() throws IOException {
        String docString = "<HTML><HEAD><META http-equiv=\"Content-Type\""
                + " content=\"text/html; charset=UTF-8\"></HEAD><BODY><SPAN id=\"testdiv\"> <a></a>"
                + "</SPAN><DIV style=\"colour:#FF0000\"><H>Header</H></DIV></BODY></HTML>";
        Document doc1 = DomUtils.asDocument(docString);

        String docString2 = "<HTML><HEAD><META http-equiv=\"Content-Type\""
                + " content=\"text/html; charset=UTF-8\"></HEAD><BODY><DIV id=\"testdiv\"> <a></a>"
                + "</DIV><DIV style=\"colour:#FF0000\"><H>Header</H></DIV></BODY></HTML>";

        Document doc2 = DomUtils.asDocument(docString2);

        double distance = HybridStateVertexImpl.computeDistance(doc1, doc2, false);

        assertEquals(distance, 1.0, 0.0);

        List<List<Node>> differentNodes = HybridStateVertexImpl.getChangedNodes(doc1, doc2, false);

        List<Node> addedNodes = differentNodes.get(0);
        List<Node> removedNodes = differentNodes.get(1);

        assertTrue(addedNodes.get(0).getNodeName().equalsIgnoreCase("span"));
        assertTrue(removedNodes.get(0).getNodeName().equalsIgnoreCase("div"));
    }

    private void loadHybridState(
            String domString, StateVertex state, FragmentManager fragmentManager, File screenshotFile)
            throws IOException {

        BufferedImage screenshot = ImageIO.read(screenshotFile);

        FragmentationPlugin.loadFragmentState(state, fragmentManager, DomUtils.asDocument(domString), screenshot);
    }

    private CandidateElement getMatchingCandidate(List<CandidateElement> elements, String xpath) {
        CandidateElement element = null;
        for (CandidateElement elem : elements) {
            if (elem.getIdentification().getValue().equalsIgnoreCase(xpath)) {
                element = elem;
            }
        }
        return element;
    }

    @Test
    public void testCandidateAddition() throws IOException {
        CrawljaxConfigurationBuilder configBuilder = CrawljaxConfiguration.builderFor("http://locahost/dummy.html");
        configBuilder.crawlRules().clickOnce(false);

        File state729 = new File("src/test/resources/crawls/frag_state610.html");
        File state802 = new File("src/test/resources/crawls/frag_state628.html");
        String docString1 = FileUtils.readFileToString(state729);
        String docString2 = FileUtils.readFileToString(state802);

        HybridStateVertexImpl state1 = new HybridStateVertexImpl(0, "", "index", docString1, docString1, 0.0, false);

        sfg = new InMemoryStateFlowGraph(new ExitNotifier(0), new FragGenStateVertexFactory(0, configBuilder, false));

        FragmentManager manager = new FragmentManager(graphProvider);

        File screenshot1 = new File("src/test/resources/crawls/state652.png");
        loadHybridState(docString1, state1, manager, screenshot1);

        File screenshot2 = new File("src/test/resources/crawls/state653.png");

        when(checker.checkCrawlCondition(browser)).thenReturn(true);

        eventableChecker = new EventableConditionChecker(configBuilder.build().getCrawlRules());
        when(checker.getEventableConditionChecker()).thenReturn(eventableChecker);

        CandidateElementExtractor extractor =
                new CandidateElementExtractor(checker, browser, handler, configBuilder.build());

        List<CandidateElement> elements = extractor.extract(state1);
        LinkedList<CandidateElement> results = new LinkedList<>();
        results.addAll(elements);
        state1.setElementsFound(results);

        Assert.assertEquals("Wrong number of candidates found", 70, elements.size());

        CandidateElement element =
                getMatchingCandidate(results, "/HTML[1]/BODY[1]/TABLE[2]/TBODY[1]/TR[1]/TD[2]/FORM[1]/INPUT[2]" + "");

        Assert.assertNotEquals(
                "A matching candidate should be found for '/HTML[1]/BODY[1]/TABLE[2]/TBODY[1]/TR[1]/TD[2]/FORM[1]/INPUT[2]'",
                null,
                element);

        Assert.assertTrue(
                "Element access should be recorded by fragment manager", manager.recordAccess(element, state1));

        HybridStateVertexImpl state2 = new HybridStateVertexImpl(1, "", "state1", docString2, docString2, 0.0, false);

        loadHybridState(docString2, state2, manager, screenshot2);

        state1.equals(state2);

        elements = extractor.extract(state2);
        results = new LinkedList<>();
        results.addAll(elements);
        Assert.assertEquals("Wrong number of candidates found", 59, elements.size());

        state2.setElementsFound(results);

        CandidateElement element2 =
                getMatchingCandidate(results, "/HTML[1]/BODY[1]/TABLE[1]/TBODY[1]/TR[1]/TD[3]/FORM[1]/INPUT[1]");

        manager.setAccess(state2);

        Assert.assertTrue(
                "Access transfer should be done for new state",
                state2.getRootFragment().isAccessTransferred());

        manager.recordAccess(element2, state2);
        Assert.assertTrue("element should be set to explored", element2.wasExplored());

        StateComparision comp = manager.cacheStateComparision(state2, state1, true);

        Assert.assertEquals("The two states are DIFFERENT", StateComparision.DIFFERENT, comp);
    }

    @Test
    public void testFragGenComparisonND2() throws IOException {
        CrawljaxConfigurationBuilder configBuilder = CrawljaxConfiguration.builderFor("http://locahost/dummy.html");

        File state729 = new File("src/test/resources/crawls/frag_state296.html");
        File state802 = new File("src/test/resources/crawls/frag_state297.html");
        String docString1 = FileUtils.readFileToString(state729);
        String docString2 = FileUtils.readFileToString(state802);

        HybridStateVertexImpl state1 = new HybridStateVertexImpl(0, "", "index", docString1, docString1, 0.0, false);
        HybridStateVertexImpl state2 = new HybridStateVertexImpl(1, "", "state1", docString2, docString2, 0.0, false);

        sfg = new InMemoryStateFlowGraph(new ExitNotifier(0), new FragGenStateVertexFactory(0, configBuilder, false));

        FragmentManager manager = new FragmentManager(graphProvider);

        File screenshot1 = new File("src/test/resources/crawls/state296.png");
        loadHybridState(docString1, state1, manager, screenshot1);

        File screenshot2 = new File("src/test/resources/crawls/state297.png");
        loadHybridState(docString2, state2, manager, screenshot2);

        Assert.assertFalse("The two states are near-duplicates, not equal", state1.equals(state2));

        StateComparision comp = manager.cacheStateComparision(state2, state1, true);

        Assert.assertEquals("The two states are NEAR_DUPLICATE2", StateComparision.NEARDUPLICATE2, comp);
    }
}
