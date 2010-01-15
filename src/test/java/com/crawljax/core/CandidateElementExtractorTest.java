package com.crawljax.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.BeforeClass;
import org.junit.Test;

import com.crawljax.browser.BrowserFactory;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.util.PropertyHelper;

public class CandidateElementExtractorTest {

	private static String url = "http://spci.st.ewi.tudelft.nl/demo/crawljax/";

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
		} catch (ConfigurationException e1) {
			e1.printStackTrace();
			fail(e1.getMessage());
		}

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
		CandidateElementExtractor extractor = new CandidateElementExtractor(crawler);
		assertNotNull(extractor);
		try {

			String inc = "a:{}";
			TagElement tagElementInc = PropertyHelper.parseTagElements(inc);
			List<TagElement> includes = new ArrayList<TagElement>();
			includes.add(tagElementInc);

			List<CandidateElement> candidates =
			        extractor.extract(includes, new ArrayList<TagElement>(), true);

			assertNotNull(candidates);
			assertEquals(15, candidates.size());

		} catch (CrawljaxException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		BrowserFactory.close();
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
		} catch (ConfigurationException e1) {
			e1.printStackTrace();
			fail(e1.getMessage());
		}

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
		CandidateElementExtractor extractor = new CandidateElementExtractor(crawler);
		assertNotNull(extractor);

		try {

			String inc = "a:{}";
			TagElement tagElementInc = PropertyHelper.parseTagElements(inc);
			List<TagElement> includes = new ArrayList<TagElement>();
			includes.add(tagElementInc);

			// now exclude some elements
			String exc = "div:{id=menubar}";

			List<TagElement> excludes = new ArrayList<TagElement>();
			TagElement tagElementExc = PropertyHelper.parseTagElements(exc);
			excludes.add(tagElementExc);

			List<CandidateElement> candidates = extractor.extract(includes, excludes, true);

			assertNotNull(candidates);
			assertEquals(11, candidates.size());

		} catch (CrawljaxException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		BrowserFactory.close();
	}
}
