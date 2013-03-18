package com.crawljax.core;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.plugin.Plugins;
import com.crawljax.core.state.StateVertex;
import com.crawljax.forms.FormHandler;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.RunWithWebServer;

@Category(BrowserTest.class)
public class CandidateElementExtractorTest {

	private static final Logger LOG = LoggerFactory
	        .getLogger(CandidateElementExtractorTest.class);
	private static final StateVertex DUMMY_STATE = new StateVertex("DUMMY", "");

	@ClassRule
	public static final RunWithWebServer DEMO_SITE_SERVER = new RunWithWebServer("/demo-site");

	private CrawljaxController controller;
	private Crawler crawler;

	@After
	public void shutDown() {
		controller.getBrowserPool().shutdown();
	}

	@Test
	public void testExtract() throws InterruptedException, CrawljaxException,
	        ConfigurationException {
		CrawljaxConfigurationBuilder builder =
		        CrawljaxConfiguration.builderFor(DEMO_SITE_SERVER.getSiteUrl().toExternalForm());
		builder.crawlRules().click("a");
		builder.crawlRules().clickOnce(true);
		CrawljaxConfiguration config = builder.build();
		setupCrawler(config);

		CandidateElementExtractor extractor = newElementExtractor(config);

		List<CandidateElement> candidates = extractor.extract(DUMMY_STATE);

		assertNotNull(candidates);
		assertEquals(15, candidates.size());

	}

	private CandidateElementExtractor newElementExtractor(CrawljaxConfiguration config) {
		FormHandler formHandler =
		        new FormHandler(crawler.getBrowser(), config.getCrawlRules()
		                .getInputSpecification(), true);

		CandidateElementExtractor extractor =
		        new CandidateElementExtractor(controller.getElementChecker(),
		                crawler.getBrowser(), formHandler, controller.getConfiguration());
		return extractor;
	}

	private void setupCrawler(CrawljaxConfiguration config) throws ConfigurationException,
	        InterruptedException {
		controller = new CrawljaxController(config);
		crawler = new CEETCrawler(controller);

		crawler.goToInitialURL();

		Thread.sleep(400);
	}

	@Test
	public void testExtractExclude() throws Exception {
		CrawljaxConfigurationBuilder builder =
		        CrawljaxConfiguration.builderFor(DEMO_SITE_SERVER.getSiteUrl().toExternalForm());
		builder.crawlRules().click("a");
		builder.crawlRules().dontClick("div").withAttribute("id", "menubar");
		builder.crawlRules().clickOnce(true);
		CrawljaxConfiguration config = builder.build();

		setupCrawler(config);

		CandidateElementExtractor extractor = newElementExtractor(config);

		List<CandidateElement> candidates = extractor.extract(DUMMY_STATE);

		assertNotNull(candidates);
		assertThat(candidates, hasSize(11));

	}

	@Test
	public void testExtractIframeContents() throws Exception {
		RunWithWebServer server = new RunWithWebServer("/site");
		server.before();
		CrawljaxConfigurationBuilder builder =
		        CrawljaxConfiguration
		                .builderFor(server.getSiteUrl().toExternalForm() + "iframe/");
		builder.crawlRules().click("a");
		CrawljaxConfiguration config = builder.build();
		setupCrawler(config);

		CandidateElementExtractor extractor = newElementExtractor(config);

		List<CandidateElement> candidates = extractor.extract(DUMMY_STATE);

		for (CandidateElement e : candidates) {
			LOG.debug("candidate: " + e.getUniqueString());
		}

		server.after();

		assertNotNull(extractor);
		assertNotNull(candidates);
		assertThat(candidates, hasSize(9));

	}

	/**
	 * Internal mock-up crawler retrieving its browser.
	 * 
	 * @author Stefan Lenselink <slenselink@google.com>
	 */
	private static class CEETCrawler extends InitialCrawler {
		private EmbeddedBrowser browser;

		/**
		 * @param mother
		 */
		public CEETCrawler(CrawljaxController mother) {
			super(mother, Plugins.noPlugins());
			try {
				browser = mother.getBrowserPool().requestBrowser();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		@Override
		public EmbeddedBrowser getBrowser() {
			return browser;
		}

	}

}
