package com.crawljax.core.largetests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.junit.Test;

import com.crawljax.condition.NotRegexCondition;
import com.crawljax.condition.NotXPathCondition;
import com.crawljax.condition.RegexCondition;
import com.crawljax.condition.XPathCondition;
import com.crawljax.condition.browserwaiter.ExpectedVisibleCondition;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.Form;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.plugin.OnInvariantViolationPlugin;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertix;
import com.crawljax.core.state.Identification.How;
import com.crawljax.oraclecomparator.comparators.DateComparator;
import com.crawljax.oraclecomparator.comparators.StyleComparator;

/**
 * This is the base abstract class for all different kind of largeTests. Sub classes tests specific
 * browser implementations like FireFox, Chrome, IE, etc.
 */
public abstract class LargeTestSuper {

	private static CrawlSession session;

	private static final int CLICKED_CLICK_ME_ELEMENTS = 6;

	private static final String CLICK_TEXT = "CLICK_ME";
	private static final String DONT_CLICK_TEXT = "DONT_CLICK_ME";
	private static final String ATTRIBUTE = "class";
	private static final String CLICK_UNDER_XPATH_ID = "CLICK_IN_HERE";
	private static final String DONT_CLICK_UNDER_XPATH_ID = "DONT_CLICK_IN_HERE";
	private static final String ILLEGAL_STATE = "FORBIDDEN_PAGE";

	private static List<Invariant> violatedInvariants = new ArrayList<Invariant>();
	private static final int VIOLATED_INVARIANTS = 1;
	private static final String VIOLATED_INVARIANT_DESCRIPTION = "expectedInvariantViolation";
	private static final String INVARIANT_TEXT = "TEST_INVARIANTS";
	private static boolean violatedInvariantStateIsCorrect = false;

	private static final RegexCondition REGEX_CONDITION_TRUE =
	        new RegexCondition("REGEX_CONDITION_TRUE");
	private static final NotRegexCondition ALLOW_BUTTON_CLICK =
	        new NotRegexCondition("DONT_CLICK_BUTTONS_ON_THIS_PAGE");

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
		assertEquals("All results are found", MULTIPLE_INPUT_RESULTS.length, resultsFound.size());
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
		assertTrue(CLICKED_CLICK_ME_ELEMENTS + " CLICK_TEXT elements are clicked ",
		        clickMeFound == CLICKED_CLICK_ME_ELEMENTS);
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
		assertTrue("Only one home page. Found: " + countHomeStates, countHomeStates == 1);
	}

	/**
	 * Tests invariants.
	 */
	@Test
	public void testInvariants() {
		// two invariants were added, but only one should fail!
		assertTrue(violatedInvariants.size() + " Invariants violated",
		        violatedInvariants.size() == VIOLATED_INVARIANTS);

		// test whether the right invariant failed
		assertTrue(VIOLATED_INVARIANT_DESCRIPTION + " failed", violatedInvariants.get(0)
		        .getDescription().equals(VIOLATED_INVARIANT_DESCRIPTION));
	}

	/**
	 * Test correct state in violatedInvariants plugin call.
	 */
	@Test
	public void testCorrectStateOnViolatedInvariants() {
		assertTrue("OnViolatedInvariantPlugin session object has the correct currentState",
		        violatedInvariantStateIsCorrect);
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

	/**
	 * retrieve / build the crawlspecification for the given arguments.
	 * 
	 * @param url
	 *            the url where the large test run is located.
	 * @param waintAfterEvent
	 *            the amount of time in ms to wait after an event is fired.
	 * @param waitAfterReload
	 *            the amount of time in ms to wait after a reload.
	 * @return the new CrawlSpecification.
	 */
	protected static CrawlSpecification getCrawlSpecification(String url, int waintAfterEvent,
	        int waitAfterReload) {

		CrawlSpecification crawler = new CrawlSpecification(url);
		crawler.setWaitTimeAfterEvent(waintAfterEvent);
		crawler.setWaitTimeAfterReloadUrl(waitAfterReload);
		crawler.setDepth(3);
		crawler.setClickOnce(true);

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
		crawler.waitFor("testWaitCondition.html", 2000, new ExpectedVisibleCondition(
		        new Identification(How.id, "SLOW_WIDGET")));
	}

	private static void addInvariants(CrawlSpecification crawler) {
		// should always fail on test invariant page
		NotXPathCondition neverDivWithInvariantViolationId =
		        new NotXPathCondition("//DIV[@id='INVARIANT_VIOLATION']");
		crawler.addInvariant(VIOLATED_INVARIANT_DESCRIPTION, neverDivWithInvariantViolationId);

		// should never fail
		RegexCondition onInvariantsPagePreCondition = new RegexCondition(INVARIANT_TEXT);
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

	/**
	 * Add the plugins to the given crawljaxConfiguration.
	 * 
	 * @param crawljaxConfiguration
	 *            the configuration to add the plugins to.
	 */
	protected static void addPlugins(CrawljaxConfiguration crawljaxConfiguration) {
		// plugin to retrieve session data
		crawljaxConfiguration.addPlugin(new PostCrawlingPlugin() {

			@Override
			public void postCrawling(CrawlSession session) {
				LargeTestSuper.session = session;

			}

		});

		crawljaxConfiguration.addPlugin(new OnInvariantViolationPlugin() {

			@Override
			public void onInvariantViolation(Invariant invariant, CrawlSession session) {
				LargeTestSuper.violatedInvariants.add(invariant);
				if (session.getCurrentState().getDom().contains(INVARIANT_TEXT)) {
					violatedInvariantStateIsCorrect = true;
					System.out.println("Invariant violated: " + invariant.getDescription());
				}
			}
		});

		crawljaxConfiguration.addPlugin(new OnNewStatePlugin() {
			@Override
			public void onNewState(CrawlSession session) {
				try {
					if (!session.getCurrentState().equals(session.getInitialState())) {
						assertEquals("Target State from ExactEventPath equals current state",
						        session.getCurrentCrawlPath().get(
						                session.getCurrentCrawlPath().size() - 1)
						                .getTargetStateVertix(), session.getCurrentState());
					}
				} catch (CrawljaxException e) {
					Assert.fail(e.getMessage());
				}
			}
		});
	}

	private StateFlowGraph getStateFlowGraph() {
		return session.getStateFlowGraph();
	}
}
