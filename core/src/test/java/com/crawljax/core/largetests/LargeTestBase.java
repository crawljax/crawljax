package com.crawljax.core.largetests;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.stateWithDomSubstring;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.crawljax.condition.NotRegexCondition;
import com.crawljax.condition.NotXPathCondition;
import com.crawljax.condition.RegexCondition;
import com.crawljax.condition.XPathCondition;
import com.crawljax.condition.browserwaiter.ExpectedVisibleCondition;
import com.crawljax.condition.browserwaiter.WaitCondition;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawlRules.CrawlRulesBuilder;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.configuration.Form;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.plugin.OnInvariantViolationPlugin;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.core.state.PostCrawlStateGraphChecker;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertex;
import com.crawljax.oraclecomparator.OracleComparator;
import com.crawljax.oraclecomparator.comparators.DateComparator;
import com.crawljax.oraclecomparator.comparators.StyleComparator;
import com.crawljax.test.RunWithWebServer;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the base abstract class for all different kind of largeTests. Sub classes tests specific
 * browser implementations like FireFox, Chrome, IE, etc.
 */
public abstract class LargeTestBase {

	private static final Logger LOG = LoggerFactory.getLogger(LargeTestBase.class);
	private static final AtomicBoolean HAS_RUN = new AtomicBoolean(false);
	private static final AtomicBoolean HAS_FINISHED = new AtomicBoolean(false);
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

	private static final String TITLE_RESULT_RANDOM_INPUT = "RESULT_RANDOM_INPUT";
	private static final String REGEX_RESULT_RANDOM_INPUT = "[a-zA-Z]{8};" + "[a-zA-Z]{8};"
	  + "(true|false);" + "(true|false);" + "OPTION[1234];" + "[a-zA-Z]{8}";

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
	private static final String[] MULTIPLE_INPUT_RESULTS = { "first;foo;true;false;OPTION1;same",
	  "second;bar;false;true;OPTION2;same", ";foo;true;false;OPTION1;same" };

	@ClassRule
	public static final RunWithWebServer WEB_SERVER = new RunWithWebServer("/site");

	@Rule
	public final Timeout timeout = new Timeout((int) TimeUnit.MINUTES.toMillis(15));

	@Before
	public void setup() throws Exception {
		if (!HAS_RUN.get()) {
			HAS_RUN.set(true);
			CrawljaxRunner crawljax = null;
			crawljax = new CrawljaxRunner(getCrawljaxConfiguration());
			session = crawljax.call();
			HAS_FINISHED.set(true);
		}
		else {
			while (!HAS_FINISHED.get()) {
				LOG.debug("Waiting for crawl to finish...");
				Thread.sleep(500);
			}
		}
	}

	/**
	 * retrieve / build the CrawljaxConfiguration for the given arguments.
	 */
	protected CrawljaxConfiguration getCrawljaxConfiguration() {

		CrawljaxConfigurationBuilder builder =
		  CrawljaxConfiguration.builderFor(WEB_SERVER.getSiteUrl());
		builder.crawlRules().waitAfterEvent(getTimeOutAfterEvent(), TimeUnit.MILLISECONDS);
		builder.crawlRules()
			   .waitAfterReloadUrl(getTimeOutAfterReloadUrl(), TimeUnit.MILLISECONDS);
		builder.setMaximumDepth(3);
		builder.crawlRules().clickOnce(true);

		builder.setBrowserConfig(getBrowserConfiguration());

		addCrawlElements(builder);

		builder.crawlRules().setInputSpec(getInputSpecification());

		addCrawlConditions(builder);
		addOracleComparators(builder);
		addInvariants(builder);
		addWaitConditions(builder);
		addPlugins(builder);

		return builder.build();
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

	private static void addWaitConditions(CrawljaxConfigurationBuilder crawler) {
		crawler.crawlRules().addWaitCondition(
		  new WaitCondition("testWaitCondition.html", 2000, new ExpectedVisibleCondition(
			new Identification(How.id, "SLOW_WIDGET"))));
	}

	private static void addInvariants(CrawljaxConfigurationBuilder builder) {
		// should always fail on test invariant page
		NotXPathCondition neverDivWithInvariantViolationId =
		  new NotXPathCondition("//DIV[@id='INVARIANT_VIOLATION']");
		builder.crawlRules().addInvariant(VIOLATED_INVARIANT_DESCRIPTION,
		  neverDivWithInvariantViolationId);

		// should never fail
		RegexCondition onInvariantsPagePreCondition = new RegexCondition(INVARIANT_TEXT);
		XPathCondition expectElement =
		  new XPathCondition("//DIV[@id='SHOULD_ALWAYS_BE_ON_THIS_PAGE']");
		builder.crawlRules().addInvariant(
		  new Invariant("testInvariantWithPrecondiions", expectElement,
			onInvariantsPagePreCondition));
	}

	private static void addCrawlElements(CrawljaxConfigurationBuilder builder) {
		CrawlRulesBuilder rules = builder.crawlRules();
		rules.click("a");
		rules.click("div").withText(CLICK_TEXT);
		rules.click("div").underXPath("//SPAN[@id='" + CLICK_UNDER_XPATH_ID + "']");
		rules.click("button").when(new NotRegexCondition("DONT_CLICK_BUTTONS_ON_THIS_PAGE"));
		rules.click("div").withAttribute(ATTRIBUTE, "condition")
			 .when(new RegexCondition("REGEX_CONDITION_TRUE"));

		rules.dontClick("a").withText(DONT_CLICK_TEXT);
		rules.dontClick("a").withAttribute(ATTRIBUTE, DONT_CLICK_TEXT);
		rules.dontClick("a").underXPath("//DIV[@id='" + DONT_CLICK_UNDER_XPATH_ID + "']");
	}

	private static void addOracleComparators(CrawljaxConfigurationBuilder builder) {
		builder.crawlRules().addOracleComparator(
		  new OracleComparator("style", new StyleComparator()));

		builder.crawlRules().addOracleComparator(
		  new OracleComparator("date", new DateComparator()));
	}

	private static void addCrawlConditions(CrawljaxConfigurationBuilder builder) {
		builder.crawlRules().addCrawlCondition("DONT_CRAWL_ME",
		  new NotRegexCondition("DONT_CRAWL_ME"));
	}

	/**
	 * Add the plugins to the given crawljaxConfiguration.
	 *
	 * @param crawljaxConfiguration the configuration to add the plugins to.
	 */
	protected static void addPlugins(CrawljaxConfigurationBuilder crawljaxConfiguration) {
		crawljaxConfiguration.addPlugin(new PostCrawlStateGraphChecker());

		crawljaxConfiguration.addPlugin(new OnInvariantViolationPlugin() {

			@Override
			public void onInvariantViolation(Invariant invariant, CrawlerContext context) {
				LargeTestBase.violatedInvariants.add(invariant);
				if (context.getBrowser().getStrippedDom().contains(INVARIANT_TEXT)) {
					violatedInvariantStateIsCorrect = true;
					LOG.warn("Invariant violated: " + invariant.getDescription());
				}
			}

		});
	}

	private StateFlowGraph getStateFlowGraph() {
		return session.getStateFlowGraph();
	}

	/**
	 * Tests random input.
	 */
	@Test
	public void testRandomFormInput() {
		for (StateVertex state : getStateFlowGraph().getAllStates()) {
			if (state.getDom().contains(TITLE_RESULT_RANDOM_INPUT)) {
				Pattern p = Pattern.compile(REGEX_RESULT_RANDOM_INPUT);
				Matcher m = p.matcher(state.getDom());
				assertTrue("Found correct random result", m.find());
				return;
			}
		}
		fail("Result random input found");
	}

	/**
	 * Test manual form input.
	 */
	@Test
	public void testManualFormInput() {
		for (StateVertex state : getStateFlowGraph().getAllStates()) {
			if (state.getDom().contains(TITLE_MANUAL_INPUT_RESULT)) {
				assertTrue("Result contains the correct data",
				  state.getDom().contains(MANUAL_INPUT_RESULT));
				return;
			}
		}
		fail("Result manual input found");
	}

	/**
	 * Tests whether all the different form values are submitted and found.
	 */
	@Test
	public void testMultipleFormInput() {
		Set<String> resultsFound = new HashSet<>();
		for (StateVertex state : getStateFlowGraph().getAllStates()) {
			if (state.getDom().contains(TITLE_MULTIPLE_INPUT_RESULT)) {
				for (String result : MULTIPLE_INPUT_RESULTS) {
					if (state.getDom().contains(result)) {
						resultsFound.add(result);
					}
				}
			}
		}
		assertThat(resultsFound, containsInAnyOrder(MULTIPLE_INPUT_RESULTS));

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
																				.getText().startsWith
				(DONT_CLICK_TEXT));
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
		assertThat(getStateFlowGraph().getAllStates(),
		  everyItem(not(stateWithDomSubstring(ILLEGAL_STATE))));
	}

	/**
	 * this tests whether the oracle comparators are working correctly the home page is different
	 * every load, but is equivalent when the oracle comparators are functioning.
	 */
	@Test
	public void testOracleComparators() {
		int countHomeStates = 0;
		for (StateVertex state : getStateFlowGraph().getAllStates()) {
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
	@Ignore("This test is non-deterministic and will fail without cause.")
	public void testInvariants() {
		// two invariants were added, but only one should fail!
		assertTrue(violatedInvariants.size() + " Invariants violated",
		  violatedInvariants.size() == VIOLATED_INVARIANTS);

		// test whether the right invariant failed
		assertTrue(VIOLATED_INVARIANT_DESCRIPTION + " failed", violatedInvariants.get(0)
																				 .getDescription()
																				 .equals(
																				   VIOLATED_INVARIANT_DESCRIPTION));
	}

	/**
	 * Test correct state in violatedInvariants plugin call.
	 */
	@Test
	@Ignore("This test is non-deterministic and will fail without cause.")
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
		for (StateVertex state : getStateFlowGraph().getAllStates()) {
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

	abstract BrowserConfiguration getBrowserConfiguration();

	abstract long getTimeOutAfterReloadUrl();

	abstract long getTimeOutAfterEvent();

}
