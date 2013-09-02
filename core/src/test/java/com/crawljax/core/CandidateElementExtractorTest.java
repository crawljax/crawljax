package com.crawljax.core;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.browser.WebDriverBrowserBuilder;
import com.crawljax.condition.ConditionTypeChecker;
import com.crawljax.condition.crawlcondition.CrawlCondition;
import com.crawljax.condition.eventablecondition.EventableConditionChecker;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.plugin.Plugins;
import com.crawljax.core.state.StateMachine;
import com.crawljax.core.state.StateVertex;
import com.crawljax.forms.FormHandler;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.RunWithWebServer;
import com.google.common.io.Resources;

@Category(BrowserTest.class)
@RunWith(MockitoJUnitRunner.class)
public class CandidateElementExtractorTest {

	private static final Logger LOG = LoggerFactory
	        .getLogger(CandidateElementExtractorTest.class);
	private static final StateVertex DUMMY_STATE = StateMachine.createIndex("http://localhost",
	        "", "");

	@Mock
	private Plugins plugins;

	@ClassRule
	public static final RunWithWebServer DEMO_SITE_SERVER = new RunWithWebServer("/demo-site");
	private EmbeddedBrowser browser;

	@After
	public void tearDown() {
		if (browser != null) {
			browser.closeOtherWindows();
			browser.close();
		}
	}

	@Test
	public void testExtract() throws InterruptedException, CrawljaxException {
		CrawljaxConfigurationBuilder builder =
		        CrawljaxConfiguration.builderFor(DEMO_SITE_SERVER.getSiteUrl().toExternalForm());
		builder.crawlRules().click("a");
		builder.crawlRules().clickOnce(true);
		CrawljaxConfiguration config = builder.build();

		CandidateElementExtractor extractor = newElementExtractor(config);
		browser.goToUrl(DEMO_SITE_SERVER.getSiteUrl());
		List<CandidateElement> candidates = extractor.extract(DUMMY_STATE);

		assertNotNull(candidates);
		assertEquals(15, candidates.size());

	}

	private CandidateElementExtractor newElementExtractor(CrawljaxConfiguration config) {
		browser = new WebDriverBrowserBuilder(config, plugins).get();
		FormHandler formHandler = new FormHandler(browser, config.getCrawlRules());

		EventableConditionChecker eventableConditionChecker =
		        new EventableConditionChecker(config.getCrawlRules());
		ConditionTypeChecker<CrawlCondition> crawlConditionChecker =
		        new ConditionTypeChecker<>(config.getCrawlRules().getPreCrawlConfig()
		                .getCrawlConditions());
		ExtractorManager checker =
		        new CandidateElementManager(eventableConditionChecker, crawlConditionChecker);
		CandidateElementExtractor extractor =
		        new CandidateElementExtractor(checker, browser, formHandler, config);

		return extractor;
	}

	@Test
	public void testExtractExclude() throws Exception {
		CrawljaxConfigurationBuilder builder =
		        CrawljaxConfiguration.builderFor(DEMO_SITE_SERVER.getSiteUrl().toExternalForm());
		builder.crawlRules().click("a");
		builder.crawlRules().dontClick("div").withAttribute("id", "menubar");
		builder.crawlRules().clickOnce(true);
		CrawljaxConfiguration config = builder.build();

		CandidateElementExtractor extractor = newElementExtractor(config);
		browser.goToUrl(DEMO_SITE_SERVER.getSiteUrl());

		List<CandidateElement> candidates = extractor.extract(DUMMY_STATE);

		assertNotNull(candidates);
		assertThat(candidates, hasSize(11));

	}

	@Test
	public void testExtractIframeContents() throws Exception {
		RunWithWebServer server = new RunWithWebServer("/site");
		server.before();
		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration
		        .builderFor(server.getSiteUrl().toExternalForm() + "iframe/");
		builder.crawlRules().click("a");
		CrawljaxConfiguration config = builder.build();

		CandidateElementExtractor extractor = newElementExtractor(config);
		browser.goToUrl(new URL(server.getSiteUrl().toExternalForm() + "iframe/"));
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
	public void whenNoFollowExternalUrlDoNotFollow() throws IOException {
		CrawljaxConfigurationBuilder builder =
		        CrawljaxConfiguration.builderFor("http://example.com");
		builder.crawlRules().click("a");
		CrawljaxConfiguration config = builder.build();
		CandidateElementExtractor extractor = newElementExtractor(config);

		List<CandidateElement> extract = extractFromTestFile(extractor);

		assertThat(config.getCrawlRules().followExternalLinks(), is(false));
		assertThat(extract, hasSize(2));
	}

	@Test
	public void whenFollowExternalUrlDoFollow() throws IOException {
		CrawljaxConfigurationBuilder builder =
		        CrawljaxConfiguration.builderFor("http://example.com");
		builder.crawlRules().click("a");
		builder.crawlRules().followExternalLinks(true);
		CrawljaxConfiguration config = builder.build();
		CandidateElementExtractor extractor = newElementExtractor(config);

		List<CandidateElement> extract = extractFromTestFile(extractor);

		assertThat(config.getCrawlRules().followExternalLinks(), is(true));
		assertThat(extract, hasSize(3));
	}

	private List<CandidateElement> extractFromTestFile(CandidateElementExtractor extractor) {
		StateVertex currentState = Mockito.mock(StateVertex.class);
		String file = "/candidateElementExtractorTest/domWithOneExternalAndTwoInternal.html";
		URL dom = Resources.getResource(getClass(), file);
		browser.goToUrl(dom);
		List<CandidateElement> extract = extractor.extract(currentState);
		return extract;
	}

}
