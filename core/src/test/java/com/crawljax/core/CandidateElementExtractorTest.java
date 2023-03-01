package com.crawljax.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.crawljax.browser.BrowserProvider;
import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.browser.WebDriverBackedEmbeddedBrowser;
import com.crawljax.clickabledetection.ClickableDetectorPlugin;
import com.crawljax.condition.ConditionTypeChecker;
import com.crawljax.condition.crawlcondition.CrawlCondition;
import com.crawljax.condition.eventablecondition.EventableConditionChecker;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.BrowserOptions;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.plugin.Plugins;
import com.crawljax.core.state.DefaultStateVertexFactory;
import com.crawljax.core.state.StateVertex;
import com.crawljax.forms.FormHandler;
import com.crawljax.stateabstractions.dom.RTEDStateVertexFactory;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.RunWithWebServer;
import com.crawljax.util.DomUtils;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Category(BrowserTest.class)
@RunWith(MockitoJUnitRunner.class)
public class CandidateElementExtractorTest {

    @ClassRule
    public static final RunWithWebServer DEMO_SITE_SERVER = new RunWithWebServer("/demo-site");

    private static final Logger LOG = LoggerFactory.getLogger(CandidateElementExtractorTest.class);
    private static StateVertex DUMMY_STATE =
            new DefaultStateVertexFactory().createIndex("http://localhost", "", "", null);

    @Rule
    public final BrowserProvider provider = new BrowserProvider();

    @Mock
    private Plugins plugins;

    private EmbeddedBrowser browser;

    @Test
    public void testExtract() throws CrawljaxException {
        CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(DEMO_SITE_SERVER.getSiteUrl());

        builder.crawlRules().click("a");
        builder.crawlRules().clickOnce(true);

        CrawljaxConfiguration config = builder.build();

        CandidateElementExtractor extractor = newElementExtractor(config, true);
        browser.goToUrl(DEMO_SITE_SERVER.getSiteUrl());
        DUMMY_STATE = new DefaultStateVertexFactory()
                .createIndex(browser.getCurrentUrl(), browser.getStrippedDom(), browser.getStrippedDom(), browser);
        List<CandidateElement> candidates = extractor.extract(DUMMY_STATE);

        assertNotNull(candidates);
        assertEquals(15, candidates.size());
    }

    @Test
    public void testExtractClickables() throws CrawljaxException, URISyntaxException, IOException {
        String url = DEMO_SITE_SERVER.getSiteUrl().toString() + "clickable/";
        CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(url);
        builder.crawlRules().clickElementsWithClickEventHandler();
        builder.crawlRules().clickOnce(true);
        BrowserOptions options = new BrowserOptions();
        options.setUSE_CDP(true);
        BrowserConfiguration browserConfiguration = new BrowserConfiguration(BrowserType.CHROME, 1, options);
        builder.setBrowserConfig(browserConfiguration);
        CrawljaxConfiguration config = builder.build();
        CandidateElementExtractor extractor = newElementExtractor(config, true);

        // Set USE CDP argument
        ((WebDriverBackedEmbeddedBrowser) browser).setUSE_CDP(true);
        browser.goToUrl(new URI(url));

        DUMMY_STATE = new RTEDStateVertexFactory(0)
                .createIndex(browser.getCurrentUrl(), browser.getStrippedDom(), browser.getStrippedDom(), browser);

        new ClickableDetectorPlugin().findClickables(browser, DUMMY_STATE);
        List<CandidateElement> candidates = extractor.extract(DUMMY_STATE);

        assertNotNull(candidates);
        assertEquals(1, candidates.size());
    }

    private CandidateElementExtractor newElementExtractor(CrawljaxConfiguration config, boolean startBrowser) {
        if (startBrowser) {
            browser = provider.newEmbeddedBrowser();
        } else {
            browser = null;
        }

        FormHandler formHandler = new FormHandler(browser, config.getCrawlRules());

        EventableConditionChecker eventableConditionChecker = new EventableConditionChecker(config.getCrawlRules());
        ConditionTypeChecker<CrawlCondition> crawlConditionChecker = new ConditionTypeChecker<>(
                config.getCrawlRules().getPreCrawlConfig().getCrawlConditions());
        ExtractorManager checker = new CandidateElementManager(eventableConditionChecker, crawlConditionChecker);

        return new CandidateElementExtractor(checker, browser, formHandler, config);
    }

    @Test
    public void testExtractExclude() {
        CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(DEMO_SITE_SERVER.getSiteUrl());
        builder.crawlRules().click("a");
        builder.crawlRules().dontClick("div").withAttribute("id", "menubar");
        builder.crawlRules().clickOnce(true);
        CrawljaxConfiguration config = builder.build();

        CandidateElementExtractor extractor = newElementExtractor(config, true);
        browser.goToUrl(DEMO_SITE_SERVER.getSiteUrl());
        DUMMY_STATE = new DefaultStateVertexFactory()
                .createIndex(browser.getCurrentUrl(), browser.getStrippedDom(), browser.getStrippedDom(), browser);
        List<CandidateElement> candidates = extractor.extract(DUMMY_STATE);

        assertNotNull(candidates);
        assertThat(candidates, hasSize(11));
    }

    @Test
    public void testExtractIframeContents() throws Exception {
        RunWithWebServer server = new RunWithWebServer("/site");
        server.before();
        CrawljaxConfigurationBuilder builder =
                CrawljaxConfiguration.builderFor(server.getSiteUrl().resolve("iframe/"));
        builder.setBrowserConfig(new BrowserConfiguration(BrowserType.CHROME));
        builder.crawlRules().click("a");
        CrawljaxConfiguration config = builder.build();

        CandidateElementExtractor extractor = newElementExtractor(config, true);
        browser.goToUrl(server.getSiteUrl().resolve("iframe/"));
        DUMMY_STATE = new DefaultStateVertexFactory()
                .createIndex(browser.getCurrentUrl(), browser.getStrippedDom(), browser.getStrippedDom(), browser);
        List<CandidateElement> candidates = extractor.extract(DUMMY_STATE);

        for (CandidateElement e : candidates) {
            LOG.debug("candidate: " + e.getUniqueString());
        }

        server.after();

        assertNotNull(extractor);
        assertNotNull(candidates);
        assertThat(candidates, hasSize(9));
    }

    @Test
    public void whenNoFollowExternalUrlDoNotFollow() throws URISyntaxException {
        CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor("http://example.com");
        builder.crawlRules().click("a");
        CrawljaxConfiguration config = builder.build();
        CandidateElementExtractor extractor = newElementExtractor(config, true);
        String file = "/candidateElementExtractorTest/domWithOneExternalAndTwoInternal.html";

        List<CandidateElement> extract = extractFromTestFile(extractor, file);

        assertThat(config.getCrawlRules().followExternalLinks(), is(false));
        assertThat(extract, hasSize(2));
    }

    @Test
    public void whenFollowExternalUrlDoFollow() throws URISyntaxException {
        CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor("http://example.com");
        builder.crawlRules().click("a");
        builder.crawlRules().followExternalLinks(true);
        CrawljaxConfiguration config = builder.build();
        CandidateElementExtractor extractor = newElementExtractor(config, true);
        String file = "/candidateElementExtractorTest/domWithOneExternalAndTwoInternal.html";

        List<CandidateElement> extract = extractFromTestFile(extractor, file);

        assertThat(config.getCrawlRules().followExternalLinks(), is(true));
        assertThat(extract, hasSize(3));
    }

    private List<CandidateElement> extractFromTestFile(CandidateElementExtractor extractor, String file)
            throws URISyntaxException {
        StateVertex currentState = Mockito.mock(StateVertex.class);
        URL dom = Resources.getResource(getClass(), file);
        browser.goToUrl(dom.toURI());
        currentState = new DefaultStateVertexFactory()
                .createIndex(browser.getCurrentUrl(), browser.getStrippedDom(), browser.getStrippedDom(), browser);
        return extractor.extract(currentState);
    }

    @Test
    public void testElementAddition() throws URISyntaxException, IOException {
        CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor("http://example.com");
        builder.crawlRules().click("a");
        builder.crawlRules().followExternalLinks(false);
        CrawljaxConfiguration config = builder.build();
        CandidateElementExtractor extractor = newElementExtractor(config, false);
        Document document = DomUtils.asDocument("");
        Element e = document.createElement("A");
        //    e.setAttribute("href", "http://www.example.com");
        document.getElementsByTagName("body").item(0).appendChild(e);
        List<CandidateElement> results = new ArrayList<>();
        extractor.extractElements(document, results, "");

        Assert.assertFalse(extractor.hrefShouldBeIgnored(e));
        Assert.assertEquals(1, results.size());
    }

    @Test
    public void testExtractShouldIgnoreDownloadFiles() throws Exception {

        CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor("http://example.com");
        builder.crawlRules().click("a");
        CrawljaxConfiguration config = builder.build();

        CandidateElementExtractor extractor = newElementExtractor(config, true);

        String file = "/candidateElementExtractorTest/domWithFourTypeDownloadLink.html";
        List<CandidateElement> candidates = extractFromTestFile(extractor, file);

        for (CandidateElement e : candidates) {
            LOG.debug("candidate: " + e.getUniqueString());
        }

        assertNotNull(candidates);
        assertEquals(12, candidates.size());
    }
}
