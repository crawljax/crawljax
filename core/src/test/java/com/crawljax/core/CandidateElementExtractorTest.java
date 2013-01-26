package com.crawljax.core;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
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

	@Test
	public void testExtract() throws InterruptedException, CrawljaxException,
	        ConfigurationException {
		CrawlSpecification spec =
		        new CrawlSpecification(DEMO_SITE_SERVER.getSiteUrl().toExternalForm());
		setupCrawler(spec);
		spec.click("a");

		FormHandler formHandler =
		        new FormHandler(crawler.getBrowser(), controller.getConfigurationReader()
		                .getInputSpecification(), true);

		CandidateElementExtractor extractor =
		        new CandidateElementExtractor(controller.getElementChecker(),
		                crawler.getBrowser(), formHandler, controller.getConfigurationReader());

		List<CandidateElement> candidates = extractor.extract(DUMMY_STATE);

		assertNotNull(candidates);
		assertEquals(15, candidates.size());

		controller.getBrowserPool().shutdown();
	}

	private void setupCrawler(CrawlSpecification spec) throws ConfigurationException,
	        InterruptedException {
		CrawljaxConfiguration config = new CrawljaxConfiguration();
		config.setCrawlSpecification(spec);
		controller = new CrawljaxController(config);
		crawler = new CEETCrawler(controller);

		crawler.goToInitialURL();
		spec.setClickOnce(true);

		Thread.sleep(400);
	}

	@Test
	public void testExtractExclude() throws Exception {
		CrawlSpecification spec =
		        new CrawlSpecification(DEMO_SITE_SERVER.getSiteUrl().toExternalForm());
		spec.click("a");
		spec.dontClick("div").withAttribute("id", "menubar");

		setupCrawler(spec);

		FormHandler formHandler =
		        new FormHandler(crawler.getBrowser(), controller.getConfigurationReader()
		                .getInputSpecification(), true);
		CandidateElementExtractor extractor =
		        new CandidateElementExtractor(controller.getElementChecker(),
		                crawler.getBrowser(), formHandler, controller.getConfigurationReader());

		List<CandidateElement> candidates = extractor.extract(DUMMY_STATE);

		assertNotNull(candidates);
		assertThat(candidates, hasSize(11));

		controller.getBrowserPool().shutdown();
	}

	@Test
	public void testExtractIframeContents() throws Exception {
		RunWithWebServer server = new RunWithWebServer("/site");
		server.before();
		CrawlSpecification spec =
		        new CrawlSpecification(server.getSiteUrl() + "iframe/");
		spec.click("a");
		setupCrawler(spec);

		FormHandler formHandler =
		        new FormHandler(crawler.getBrowser(), controller.getConfigurationReader()
		                .getInputSpecification(), true);
		CandidateElementExtractor extractor =
		        new CandidateElementExtractor(controller.getElementChecker(),
		                crawler.getBrowser(), formHandler, controller.getConfigurationReader());
		assertNotNull(extractor);

		List<CandidateElement> candidates = extractor.extract(DUMMY_STATE);

		for (CandidateElement e : candidates) {
			LOG.debug("candidate: " + e.getUniqueString());
		}

		assertNotNull(candidates);
		assertThat(candidates, hasSize(9));

		controller.getBrowserPool().shutdown();
		server.after();
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
			super(mother);
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
