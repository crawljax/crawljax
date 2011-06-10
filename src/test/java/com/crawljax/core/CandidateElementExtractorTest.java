package com.crawljax.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.BeforeClass;
import org.junit.Test;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.state.StateVertix;
import com.crawljax.forms.FormHandler;

public class CandidateElementExtractorTest {

	private static String url = "http://spci.st.ewi.tudelft.nl/demo/crawljax/";
	private static final StateVertix DUMMY_STATE = new StateVertix("DUMMY", "");

	@BeforeClass
	public static void startup() {

	}

	@Test
	public void testExtract() {
		CrawljaxConfiguration config = new CrawljaxConfiguration();
		CrawlSpecification spec = new CrawlSpecification(url);
		config.setCrawlSpecification(spec);
		Crawler crawler = null;
		try {
			CrawljaxController controller = new CrawljaxController(config);
			crawler = new CEETCrawler(controller);

			assertNotNull(crawler);

			crawler.goToInitialURL();

			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			FormHandler formHandler =
			        new FormHandler(crawler.getBrowser(), controller.getConfigurationReader()
			                .getInputSpecification(), true);
			CandidateElementExtractor extractor =
			        new CandidateElementExtractor(controller.getElementChecker(),
			                crawler.getBrowser(), formHandler, controller
			                        .getConfigurationReader().getCrawlSpecificationReader());
			assertNotNull(extractor);
			try {

				TagElement tagElementInc = new TagElement(null, "a");
				List<TagElement> includes = new ArrayList<TagElement>();
				includes.add(tagElementInc);

				List<CandidateElement> candidates =
				        extractor.extract(includes, new ArrayList<TagElement>(), true,
				                DUMMY_STATE);

				assertNotNull(candidates);
				assertEquals(15, candidates.size());

			} catch (CrawljaxException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			controller.getBrowserPool().shutdown();
		} catch (ConfigurationException e1) {
			e1.printStackTrace();
			fail(e1.getMessage());
		}
	}

	@Test
	public void testExtractExclude() {
		CrawljaxConfiguration config = new CrawljaxConfiguration();
		CrawlSpecification spec = new CrawlSpecification(url);
		config.setCrawlSpecification(spec);
		Crawler crawler = null;
		try {
			CrawljaxController controller = new CrawljaxController(config);
			crawler = new CEETCrawler(controller);

			assertNotNull(crawler);

			crawler.goToInitialURL();

			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			FormHandler formHandler =
			        new FormHandler(crawler.getBrowser(), controller.getConfigurationReader()
			                .getInputSpecification(), true);
			CandidateElementExtractor extractor =
			        new CandidateElementExtractor(controller.getElementChecker(),
			                crawler.getBrowser(), formHandler, controller
			                        .getConfigurationReader().getCrawlSpecificationReader());
			assertNotNull(extractor);

			try {

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

			} catch (CrawljaxException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			controller.getBrowserPool().shutdown();
		} catch (ConfigurationException e1) {
			e1.printStackTrace();
			fail(e1.getMessage());
		}
	}

	@Test
	public void testExtractIframeContents() {
		CrawljaxConfiguration config = new CrawljaxConfiguration();
		File index = new File("src/test/site/iframe/index.html");
		CrawlSpecification spec = new CrawlSpecification("file://" + index.getAbsolutePath());

		config.setCrawlSpecification(spec);
		Crawler crawler = null;
		try {
			CrawljaxController controller = new CrawljaxController(config);
			crawler = new CEETCrawler(controller);

			assertNotNull(crawler);

			crawler.goToInitialURL();

			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			FormHandler formHandler =
			        new FormHandler(crawler.getBrowser(), controller.getConfigurationReader()
			                .getInputSpecification(), true);
			CandidateElementExtractor extractor =
			        new CandidateElementExtractor(controller.getElementChecker(),
			                crawler.getBrowser(), formHandler, controller
			                        .getConfigurationReader().getCrawlSpecificationReader());
			assertNotNull(extractor);
			try {

				TagElement tagElementInc = new TagElement(null, "a");
				List<TagElement> includes = new ArrayList<TagElement>();
				includes.add(tagElementInc);

				List<CandidateElement> candidates =
				        extractor.extract(includes, new ArrayList<TagElement>(), true,
				                DUMMY_STATE);

				for (CandidateElement e : candidates) {
					System.out.println("candidate: " + e.getUniqueString());
				}

				assertNotNull(candidates);
				assertEquals(9, candidates.size());

			} catch (CrawljaxException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			controller.getBrowserPool().shutdown();

		} catch (ConfigurationException e1) {
			e1.printStackTrace();
			fail(e1.getMessage());
		}
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
