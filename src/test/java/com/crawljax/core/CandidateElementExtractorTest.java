package com.crawljax.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.BeforeClass;
import org.junit.Test;

import com.crawljax.browser.BrowserFactory;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.state.StateVertix;
import com.crawljax.util.PropertyHelper;

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
			crawler = controller.getCrawler();

			assertNotNull(crawler);

			try {
				crawler.goToInitialURL();
			} catch (CrawljaxException e1) {
				e1.printStackTrace();
				fail(e1.getMessage());
			}

			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			CandidateElementExtractor extractor =
			        new CandidateElementExtractor(controller.getElementChecker(), crawler
			                .getBrowser());
			assertNotNull(extractor);
			try {

				String inc = "a:{}";
				TagElement tagElementInc = PropertyHelper.parseTagElement(inc);
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
			BrowserFactory.close();
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
			crawler = controller.getCrawler();

			assertNotNull(crawler);

			try {
				crawler.goToInitialURL();
			} catch (CrawljaxException e1) {
				e1.printStackTrace();
				fail(e1.getMessage());
			}

			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			CandidateElementExtractor extractor =
			        new CandidateElementExtractor(controller.getElementChecker(), crawler
			                .getBrowser());
			assertNotNull(extractor);

			try {

				String inc = "a:{}";
				TagElement tagElementInc = PropertyHelper.parseTagElement(inc);
				List<TagElement> includes = new ArrayList<TagElement>();
				includes.add(tagElementInc);

				// now exclude some elements
				String exc = "div:{id=menubar}";

				List<TagElement> excludes = new ArrayList<TagElement>();
				TagElement tagElementExc = PropertyHelper.parseTagElement(exc);
				excludes.add(tagElementExc);

				List<CandidateElement> candidates =
				        extractor.extract(includes, excludes, true, DUMMY_STATE);

				assertNotNull(candidates);
				assertEquals(11, candidates.size());

			} catch (CrawljaxException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			BrowserFactory.close();
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
			crawler = controller.getCrawler();

			assertNotNull(crawler);

			try {
				crawler.goToInitialURL();
			} catch (CrawljaxException e1) {
				e1.printStackTrace();
				fail(e1.getMessage());
			}

			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			CandidateElementExtractor extractor =
			        new CandidateElementExtractor(controller.getElementChecker(), crawler
			                .getBrowser());
			assertNotNull(extractor);
			try {
				String inc = "a:{}";
				TagElement tagElementInc = PropertyHelper.parseTagElement(inc);
				List<TagElement> includes = new ArrayList<TagElement>();
				includes.add(tagElementInc);

				List<CandidateElement> candidates =
				        extractor.extract(includes, new ArrayList<TagElement>(), true,
				                DUMMY_STATE);

				for (CandidateElement e : candidates) {
					System.out.println("candidate: " + e.getUniqueString());
				}

				assertNotNull(candidates);
				assertEquals(8, candidates.size());

			} catch (CrawljaxException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			BrowserFactory.close();
		} catch (ConfigurationException e1) {
			e1.printStackTrace();
			fail(e1.getMessage());
		}
	}
}
