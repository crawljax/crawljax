package com.crawljax.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.browser.WebDriverFirefox;
import com.crawljax.condition.eventablecondition.EventableConditionChecker;
import com.crawljax.util.PropertyHelper;

public class CandidateElementExtractorTest {

	private static EmbeddedBrowser browser;
	private static String url = "http://spci.st.ewi.tudelft.nl/demo/crawljax/";

	@BeforeClass
	public static void startup() {
		browser = new WebDriverFirefox();

		try {
			browser.goToUrl(url);
		} catch (CrawljaxException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testExtract() {
		CandidateElementExtractor extractor =
		        new CandidateElementExtractor(browser, new EventableConditionChecker());
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
	}

	@Test
	public void testExtractExclude() {
		CandidateElementExtractor extractor =
		        new CandidateElementExtractor(browser, new EventableConditionChecker());
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
	}

	@AfterClass
	public static void closeup() {
		browser.close();
	}
}
