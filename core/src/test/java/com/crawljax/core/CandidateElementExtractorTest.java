package com.crawljax.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	public static final RunWithWebServer SERVER = new RunWithWebServer("/demo-site");

	private CrawljaxController controller;
	private Crawler crawler;

	@Test
	public void testExtract() throws InterruptedException, CrawljaxException,
	        ConfigurationException {
		setupCrawler(new CrawlSpecification(SERVER.getSiteUrl().toExternalForm()));

		FormHandler formHandler =
		        new FormHandler(crawler.getBrowser(), controller.getConfigurationReader()
		                .getInputSpecification(), true);
		CandidateElementExtractor extractor =
		        new CandidateElementExtractor(controller.getElementChecker(),
		                crawler.getBrowser(), formHandler, controller.getConfigurationReader()
		                        .getCrawlSpecificationReader());
		assertNotNull(extractor);

		TagElement tagElementInc = new TagElement(null, "a");
		List<TagElement> includes = new ArrayList<TagElement>();
		includes.add(tagElementInc);

		List<CandidateElement> candidates =
		        extractor.extract(includes, new ArrayList<TagElement>(), true, DUMMY_STATE);

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

		assertNotNull(crawler);

		crawler.goToInitialURL();

		Thread.sleep(400);
	}

	@Test
	public void testExtractExclude() throws Exception {
		setupCrawler(new CrawlSpecification(SERVER.getSiteUrl().toExternalForm()));

		FormHandler formHandler =
		        new FormHandler(crawler.getBrowser(), controller.getConfigurationReader()
		                .getInputSpecification(), true);
		CandidateElementExtractor extractor =
		        new CandidateElementExtractor(controller.getElementChecker(),
		                crawler.getBrowser(), formHandler, controller.getConfigurationReader()
		                        .getCrawlSpecificationReader());
		assertNotNull(extractor);

		TagElement tagElementInc = new TagElement(null, "a");
		List<TagElement> includes = new ArrayList<TagElement>();
		includes.add(tagElementInc);

		List<TagElement> excludes = new ArrayList<TagElement>();
		TagAttribute attr = new TagAttribute("id", "menubar");
		Set<TagAttribute> attributes = new HashSet<TagAttribute>();
		attributes.add(attr);
		TagElement tagElementExc = new TagElement(attributes, "div");
		excludes.add(tagElementExc);

		List<CandidateElement> candidates =
		        extractor.extract(includes, excludes, true, DUMMY_STATE);

		assertNotNull(candidates);
		assertEquals(11, candidates.size());

		controller.getBrowserPool().shutdown();
	}

	@Test
	public void testExtractIframeContents() throws Exception {
		File index = new File("src/test/resources/site/iframe/index.html");
		CrawlSpecification spec = new CrawlSpecification("file://" + index.getAbsolutePath());
		setupCrawler(spec);

		FormHandler formHandler =
		        new FormHandler(crawler.getBrowser(), controller.getConfigurationReader()
		                .getInputSpecification(), true);
		CandidateElementExtractor extractor =
		        new CandidateElementExtractor(controller.getElementChecker(),
		                crawler.getBrowser(), formHandler, controller.getConfigurationReader()
		                        .getCrawlSpecificationReader());
		assertNotNull(extractor);

		TagElement tagElementInc = new TagElement(null, "a");
		List<TagElement> includes = new ArrayList<TagElement>();
		includes.add(tagElementInc);

		List<CandidateElement> candidates =
		        extractor.extract(includes, new ArrayList<TagElement>(), true, DUMMY_STATE);

		for (CandidateElement e : candidates) {
			LOG.debug("candidate: " + e.getUniqueString());
		}

		assertNotNull(candidates);
		assertEquals(9, candidates.size());

		controller.getBrowserPool().shutdown();

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
