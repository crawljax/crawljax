package com.crawljax.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;

import com.crawljax.browser.WebDriverIE;
import com.crawljax.condition.NotRegexCondition;
import com.crawljax.condition.NotXPathCondition;
import com.crawljax.condition.RegexCondition;
import com.crawljax.condition.XPathCondition;
import com.crawljax.condition.browserwaiter.ExpectedVisibleCondition;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.Form;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.plugin.OnInvariantViolationPlugin;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertix;
import com.crawljax.oraclecomparator.comparators.DateComparator;
import com.crawljax.oraclecomparator.comparators.StyleComparator;

/**
 * Large test for Crawljax. Crawls a test site and then inspects whether it is crawled correctly See
 * src/test/site for the web site
 * 
 * @author Danny Roest
 * @author mesbah
 * @version $Id$
 */
public class LargeCrawljaxTestIE {

	private static CrawlSession session;

	private static final int CLICKED_CLICK_ME_ELEMENTS = 6;
	private static final String URL_SITE = "http://spci.st.ewi.tudelft.nl/demo/testsite/";
	private static final String CLICK_TEXT = "CLICK_ME";
	private static final String DONT_CLICK_TEXT = "DONT_CLICK_ME";
	private static final String ATTRIBUTE = "class";
	private static final String CLICK_UNDER_XPATH_ID = "CLICK_IN_HERE";
	private static final String DONT_CLICK_UNDER_XPATH_ID = "DONT_CLICK_IN_HERE";
	private static final String ILLEGAL_STATE = "FORBIDDEN_PAGE";

	private static List<Invariant> violatedInvariants = new ArrayList<Invariant>();
	private static final int VIOLATED_INVARIANTS = 1;
	private static final String VIOLATED_INVARIANT_DESCRIPTION = "expectedInvariantViolation";

	private static final RegexCondition REGEX_CONDITION_TRUE =
	        new RegexCondition("REGEX_CONDITION_TRUE");
	private static final NotRegexCondition ALLOW_BUTTON_CLICK =
	        new NotRegexCondition("DONT_CLICK_BUTTONS_ON_THIS_PAGE");

	private static final String INDEX = "src/test/site/index.html";

	private static final String TITLE_RESULT_RANDOM_INPUT = "RESULT_RANDOM_INPUT";
	private static final String REGEX_RESULT_RANDOM_INPUT =
	        "[a-zA-Z]{8};" + "[a-zA-Z]{8};" + "(true|false);" + "(true|false);" + "OPTION[1234];"
	                + "[a-zA-Z]{8}";

	// manual values
	private static final String TITLE_MANUAL_INPUT_RESULT = "RESULT_MANUAL_INPUT";
	private static final String MANUAL_INPUT_TEXT = "foo";
	private static final String MANUAL_INPUT_TEXT2 = "crawljax";
	private static final boolean MANUAL_INPUT_CHECKBOX = true;
	private static final boolean MANUAL_INPUT_RADIO = false;
	private static final String MANUAL_INPUT_SELECT = "OPTION4";
	private static final String MANUAL_INPUT_TEXTAREA = "bar";
	private static final String MANUAL_INPUT_RESULT = "foo;crawljax;true;false;OPTION4;bar";

	// multiple values
	private static final String[] MULTIPLE_INPUT_TEXT = { "first", "second", "" };
	private static final String[] MULTIPLE_INPUT_TEXT2 = { "foo", "bar" };
	private static final boolean[] MULTIPLE_INPUT_CHECKBOX = { true, false };
	private static final boolean[] MULTIPLE_INPUT_RADIO = { false, true };
	private static final String[] MULTIPLE_INPUT_SELECT = { "OPTION1", "OPTION2" };
	private static final String[] MULTIPLE_INPUT_TEXTAREA = { "same" };

	private static final String TITLE_MULTIPLE_INPUT_RESULT = "RESULT_MULTIPLE_INPUT";
	private static final String[] MULTIPLE_INPUT_RESULTS =
	        { "first;foo;true;false;OPTION1;same", "second;bar;false;true;OPTION2;same",
	                ";foo;true;false;OPTION1;same" };

	/**
	 * Runs crawljax.
	 * 
	 * @throws java.lang.Exception
	 *             when error while crawling
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
			System.out.println("IE cannot be tested on this platform!");
			System.exit(1);
		}
		CrawljaxConfiguration crawljaxConfiguration = new CrawljaxConfiguration();
		crawljaxConfiguration.setCrawlSpecification(getCrawlSpecification());
		crawljaxConfiguration.setBrowser(new WebDriverIE());
		addPlugins(crawljaxConfiguration);
		CrawljaxController crawljax = new CrawljaxController(crawljaxConfiguration);
		crawljax.run();
	}

	/**
	 * Tests random input.
	 */
	@Test
	public void testRandomFormInput() {
		for (StateVertix state : getStateFlowGraph().getAllStates()) {
			if (state.getDom().contains(TITLE_RESULT_RANDOM_INPUT)) {
				Pattern p = Pattern.compile(REGEX_RESULT_RANDOM_INPUT);
				Matcher m = p.matcher(state.getDom());
				assertTrue("Found correct random result", m.find());
				return;
			}
		}
		// should never reach this point
		assertTrue("Result random input found", false);
	}

	/**
	 * Test manual form input.
	 */
	@Test
	public void testManualFormInput() {
		for (StateVertix state : getStateFlowGraph().getAllStates()) {
			if (state.getDom().contains(TITLE_MANUAL_INPUT_RESULT)) {
				assertTrue("Result contains the correct data", state.getDom().contains(
				        MANUAL_INPUT_RESULT));
				return;
			}
		}
		// should never reach this point
		assertTrue("Result manual input found", false);
	}

	/**
	 * Tests whether all the different form values are submitted and found.
	 */
	@Test
	public void testMultipleFormInput() {
		List<String> resultsFound = new ArrayList<String>();
		for (StateVertix state : getStateFlowGraph().getAllStates()) {
			if (state.getDom().contains(TITLE_MULTIPLE_INPUT_RESULT)) {
				for (String result : MULTIPLE_INPUT_RESULTS) {
					if (state.getDom().contains(result) && !resultsFound.contains(result)) {
						resultsFound.add(result);
					}
				}
			}
		}
		assertEquals(MULTIPLE_INPUT_RESULTS.length, resultsFound.size());
	}

	/**
	 * Checks the eventables for illegal eventables.
	 */
	@Test
	public void testCrawledElements() {
		int clickMeFound = 0;
		for (Eventable eventable : getStateFlowGraph().getAllEdges()) {

			// elements with DONT_CLICK_TEXT should never be clicked
			assertTrue("No illegal element is clicked: " + eventable, !eventable.getElement()
			        .getText().startsWith(DONT_CLICK_TEXT));
			if (eventable.getElement().getText().startsWith(CLICK_TEXT)) {
				clickMeFound++;
			}
		}
		assertEquals(CLICKED_CLICK_ME_ELEMENTS, clickMeFound);
	}

	/**
	 * checks whether there are any illegal states.
	 */
	@Test
	public void testForIllegalStates() {
		for (StateVertix state : getStateFlowGraph().getAllStates()) {
			assertTrue("Only legal states: " + state.getName(), !state.getDom().contains(
			        ILLEGAL_STATE));
		}
	}

	/**
	 * this tests whether the oracle comparators are working correctly the home page is different
	 * every load, but is equivalent when the oracle comparators are functioning.
	 */
	@Test
	public void testOracleComparators() {
		int countHomeStates = 0;
		for (StateVertix state : getStateFlowGraph().getAllStates()) {
			if (state.getDom().contains("HOMEPAGE")) {
				countHomeStates++;
			}
		}
		assertEquals("Only one home page. Found: " + countHomeStates, 1, countHomeStates);
	}

	/**
	 * Tests invariants.
	 */
	@Test
	public void testInvariants() {
		// two invariants were added, but only one should fail!
		assertEquals(VIOLATED_INVARIANTS + " Invariants violated", VIOLATED_INVARIANTS,
		        violatedInvariants.size());
		// test whether the right invariant failed
		assertTrue(VIOLATED_INVARIANT_DESCRIPTION + " failed", violatedInvariants.get(0)
		        .getDescription().equals(VIOLATED_INVARIANT_DESCRIPTION));
	}

	/**
	 * Tests waitconditions with a slow widget.
	 */
	@Test
	public void testWaitCondition() {
		boolean foundSlowWidget = false;
		for (StateVertix state : getStateFlowGraph().getAllStates()) {
			if (state.getDom().contains("TEST_WAITCONDITION")
			        && state.getDom().contains("LOADED_SLOW_WIDGET")) {
				foundSlowWidget = true;
			}
		}
		assertTrue("SLOW_WIDGET is found", foundSlowWidget);
		boolean foundLinkInSlowWidget = false;
		for (Eventable eventable : getStateFlowGraph().getAllEdges()) {
			if (eventable.getElement().getText().equals("SLOW_WIDGET_HOME")) {
				foundLinkInSlowWidget = true;
			}
		}
		assertTrue("Link in SLOW_WIDGET is found", foundLinkInSlowWidget);
	}

	/**
	 * Tests the limit for the Crawl Depth. The current crawl depth in this test is limited to 3! It
	 * test a not to deep path (path a), a to deep path (path b), a path which has a CLONE (path c),
	 * a to deep path after a CLONE found (path d), a to deep path with a branch in it (path e), a
	 * to deep path with a nop operations that results in a DOM is not changed event. The test was
	 * created after a bug found when the depth limit was not applied after a CLONE has been
	 * detected.
	 */
	@Test
	public void testDepth() {
		boolean crawlToDeep = false;
		int level1 = 0;
		int level2 = 0;
		for (Eventable eventable : getStateFlowGraph().getAllEdges()) {
			String txt = eventable.getElement().getText();
			if (txt.startsWith("Depth")) {
				// Its a depth eventable were interested in that!
				String lastPart = txt.substring(5);
				int nr = Integer.valueOf(lastPart.substring(lastPart.length() - 1));
				// String id = lastPart.substring(0, lastPart.length() - 1);
				if (nr == 1) {
					level1++;
				} else if (nr == 2) {
					level2++;
				} else {
					crawlToDeep = true;
				}
			}
		}
		assertTrue("Crawling was to deep, not limited by the setDepth parameter", !crawlToDeep);
		assertTrue("Too many nodes found at level 1 number of nodes: " + level1 + " Required: "
		        + 6, level1 <= 6);
		assertTrue("Too less nodes found at level 1 number of nodes: " + level1 + " Required: "
		        + 6, level1 >= 6);
		assertTrue("Too many nodes found at level 2 number of nodes: " + level2 + " Required: "
		        + 5, level2 <= 5);
		assertTrue("Too less nodes found at level 2 number of nodes: " + level2 + " Required: "
		        + 5, level2 >= 5);
	}

	/* setting up */

	private static CrawlSpecification getCrawlSpecification() {

		CrawlSpecification crawler = new CrawlSpecification(URL_SITE);
		crawler.setWaitTimeAfterEvent(800);
		crawler.setWaitTimeAfterReloadUrl(400);
		crawler.setDepth(3);

		addCrawlElements(crawler);

		crawler.setInputSpecification(getInputSpecification());

		addCrawlConditions(crawler);
		addOracleComparators(crawler);
		addInvariants(crawler);
		addWaitConditions(crawler);

		return crawler;
	}

	private static InputSpecification getInputSpecification() {
		InputSpecification input = new InputSpecification();
		input.field("textManual").setValue(MANUAL_INPUT_TEXT);
		input.field("text2Manual").setValue(MANUAL_INPUT_TEXT2);
		input.field("checkboxManual").setValue(MANUAL_INPUT_CHECKBOX);
		input.field("radioManual").setValue(MANUAL_INPUT_RADIO);
		input.field("selectManual").setValue(MANUAL_INPUT_SELECT);
		input.field("textareaManual").setValue(MANUAL_INPUT_TEXTAREA);

		Form form = new Form();
		form.field("textMultiple").setValues(MULTIPLE_INPUT_TEXT);
		form.field("text2Multiple").setValues(MULTIPLE_INPUT_TEXT2);
		form.field("checkboxMultiple").setValues(MULTIPLE_INPUT_CHECKBOX);
		form.field("radioMultiple").setValues(MULTIPLE_INPUT_RADIO);
		form.field("selectMultiple").setValues(MULTIPLE_INPUT_SELECT);
		form.field("textareaMultiple").setValues(MULTIPLE_INPUT_TEXTAREA);
		input.setValuesInForm(form).beforeClickElement("a").withText("Submit Multiple");
		return input;
	}

	private static void addWaitConditions(CrawlSpecification crawler) {
		crawler.waitFor("testWaitCondition.html", 2000, new ExpectedVisibleCondition(By
		        .id("SLOW_WIDGET")));
	}

	private static void addInvariants(CrawlSpecification crawler) {
		// should always fail on test invariant page
		NotXPathCondition neverDivWithInvariantViolationId =
		        new NotXPathCondition("//DIV[@id='INVARIANT_VIOLATION']");
		crawler.addInvariant(VIOLATED_INVARIANT_DESCRIPTION, neverDivWithInvariantViolationId);

		// should never fail
		RegexCondition onInvariantsPagePreCondition = new RegexCondition("TEST_INVARIANTS");
		XPathCondition expectElement =
		        new XPathCondition("//DIV[@id='SHOULD_ALWAYS_BE_ON_THIS_PAGE']");
		crawler.addInvariant("testInvariantWithPrecondiions", expectElement,
		        onInvariantsPagePreCondition);
	}

	private static void addCrawlElements(CrawlSpecification crawler) {
		crawler.click("a");
		crawler.click("div").withText(CLICK_TEXT);
		crawler.click("div").underXPath("//SPAN[@id='" + CLICK_UNDER_XPATH_ID + "']");
		crawler.when(ALLOW_BUTTON_CLICK).click("button");
		crawler.when(REGEX_CONDITION_TRUE).click("div").withAttribute(ATTRIBUTE, "condition");

		crawler.dontClick("a").withText(DONT_CLICK_TEXT);
		crawler.dontClick("a").withAttribute(ATTRIBUTE, DONT_CLICK_TEXT);
		crawler.dontClick("a").underXPath("//DIV[@id='" + DONT_CLICK_UNDER_XPATH_ID + "']");
	}

	private static void addOracleComparators(CrawlSpecification crawler) {
		crawler.addOracleComparator("style", new StyleComparator());
		crawler.addOracleComparator("date", new DateComparator());
	}

	private static void addCrawlConditions(CrawlSpecification crawler) {
		crawler.addCrawlCondition("DONT_CRAWL_ME", new NotRegexCondition("DONT_CRAWL_ME"));
	}

	private static void addPlugins(CrawljaxConfiguration crawljaxConfiguration) {
		// plugin to retrieve session data
		crawljaxConfiguration.addPlugin(new PostCrawlingPlugin() {

			@Override
			public void postCrawling(CrawlSession session) {
				LargeCrawljaxTestIE.session = session;

			}

		});

		crawljaxConfiguration.addPlugin(new OnInvariantViolationPlugin() {

			@Override
			public void onInvariantViolation(Invariant invariant, CrawlSession session) {
				LargeCrawljaxTestIE.violatedInvariants.add(invariant);
			}
		});
	}

	private StateFlowGraph getStateFlowGraph() {
		return session.getStateMachine().getStateFlowGraph();
	}

}
