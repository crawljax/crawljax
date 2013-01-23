package com.crawljax.core.configuration;

import java.util.List;

import com.crawljax.condition.Condition;
import com.crawljax.condition.browserwaiter.ExpectedCondition;
import com.crawljax.condition.browserwaiter.WaitCondition;
import com.crawljax.condition.crawlcondition.CrawlCondition;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.oraclecomparator.Comparator;
import com.crawljax.oraclecomparator.OracleComparator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Specifies the crawl options for a single crawl session. The user must specify which HTML elements
 * should be clicked during the crawl session.
 * <p/>
 * The scope can be restricted using {@link #setDepth(int)}and {@link #setMaximumStates(int)}.<br />
 * The duration can be restricted using {@link #setMaximumRuntime(int)}.
 * <p/>
 * By default Crawljax fills in random values for input fields
 * {@link #setRandomInputInForms(boolean)}.<br />
 * Specific input for form elements can be defined with
 * {@link #setInputSpecification(InputSpecification)}.<br />
 * Default values: Maximum runtime: 3600 seconds<br />
 * Time to wait after initial pageload: 500 milliseconds<br />
 * Time to wait after clicking HTML elements: 500 milliseconds<br />
 * Enter random form input data: true
 * <p/>
 * EXAMPLE:<br />
 * CrawlSpecification crawler = new CrawlSpecification("http://www.google.com");<br />
 * //click these elements<br />
 * crawler.click("a");<br />
 * crawler.click("input").withAttribute("type", "submit");<br />
 * onLoginPageCondition = new UrlCondition("#login");<br />
 * crawler.when(onLoginPageCondition).click("a").withText("Login");<br />
 * //but don't click these<br />
 * crawler.dontClick("a").underXpath("//DIV[@id='guser']");
 * crawler.dontClick("a").withText("Language Tools"); <br />
 * //restrict the scope of the crawling<br />
 * crawler.setCrawlMaximumStates(15);<br />
 * crawler.setCrawlDepth(2);
 * 
 * @author DannyRoest@gmail.com (Danny Roest)
 * @version $Id$
 */
public class CrawlSpecification {

	private static final int DEFAULT_MAXIMUMRUNTIME = 3600;
	private static final int DEFAULT_WAITTIMEAFTERRELOADURL = 500;
	private static final int DEFAULT_WAITTIMEAFTEREVENT = 500;

	private final String url;

	private final List<EventType> crawlEvents = Lists.newLinkedList();
	private final List<String> ignoredFrameIdentifiers = Lists.newLinkedList();
	private final List<Invariant> invariants = Lists.newLinkedList();
	private final List<OracleComparator> oracleComparators = Lists.newLinkedList();
	private final List<WaitCondition> waitConditions = Lists.newLinkedList();
	private final List<CrawlCondition> crawlConditions = Lists.newLinkedList();

	private int depth = 2;
	private int maximumStates = 0;
	private int maximumRuntime = DEFAULT_MAXIMUMRUNTIME; // in seconds
	private int waitTimeAfterReloadUrl = DEFAULT_WAITTIMEAFTERRELOADURL; // in milliseconds
	private int waitTimeAfterEvent = DEFAULT_WAITTIMEAFTEREVENT; // in milliseconds
	private final CrawlActions crawlActions = new CrawlActions();

	private boolean randomInputInForms = true;
	private InputSpecification inputSpecification = new InputSpecification();

	private boolean testInvariantsWhileCrawling = true;

	private boolean clicklOnce = true;
	private boolean disableCrawlFrames = false;

	/**
	 * @param url
	 *            the site to crawl
	 */
	public CrawlSpecification(String url) {
		this.crawlEvents.add(EventType.click);
		this.url = url;
	}

	/**
	 * Specifies that Crawljax should click all the default clickable elements. These include: All
	 * anchor tags All buttons
	 */
	public void clickDefaultElements() {
		crawlActions.click("a");
		crawlActions.click("button");
		crawlActions.click("input").withAttribute("type", "submit");
		crawlActions.click("input").withAttribute("type", "button");
	}

	/**
	 * Guifre Ruiz: This method can be used to crawl more tags and, therefore, more pages in the
	 * target. However, it slow down a bit the process.
	 */
	public void clickMoreElements() {
		crawlActions.click("a");
		crawlActions.click("button");
		crawlActions.click("td");
		crawlActions.click("span");
		crawlActions.click("div");
		crawlActions.click("tr");
		crawlActions.click("table");
		crawlActions.click("tbody");
		crawlActions.click("ol");
		crawlActions.click("center");
		crawlActions.click("li");
		crawlActions.click("radio");
		crawlActions.click("non");
		crawlActions.click("meta");
		crawlActions.click("refresh");
		crawlActions.click("xhr");
		crawlActions.click("relative");
		crawlActions.click("link");
		crawlActions.click("self");
		crawlActions.click("form");
		crawlActions.click("input");
		crawlActions.click("option");
		crawlActions.click("img");
		crawlActions.click("p");
	}

	/**
	 * Set of HTML elements Crawljax will click during crawling For exmple 1) <a.../> 2) <div/>
	 * click("a") will only include 1 This set can be restricted by {@link #dontClick(String)}.
	 * 
	 * @param tagName
	 *            the tag name of the elements to be included
	 * @return this CrawlElement
	 */
	public CrawlElement click(String tagName) {
		return crawlActions.click(tagName);
	}

	/**
	 * Set of HTML elements Crawljax will NOT examine during crawling When an HTML is present in the
	 * click and dontClick sets, then the element will not be clicked. For example: 1) <a
	 * href="#">Some text</a> 2) <a class="foo" .../> 3) <div class="foo" .../> click("a")
	 * dontClick("a").withAttribute("class", "foo"); Will include only include HTML element 2
	 * 
	 * @param tagName
	 *            the tag name of the elements to be excluded
	 * @return this CrawlElement
	 */
	public CrawlElement dontClick(String tagName) {
		return crawlActions.dontClick(tagName);
	}

	/**
	 * @return the initial url of the site to crawl
	 */
	protected String getUrl() {
		return url;
	}

	/**
	 * @return the maximum crawl depth
	 */
	protected int getDepth() {
		return depth;
	}

	/**
	 * Sets the maximum crawl depth. 1 is one click, 2 is two clicks deep, ...
	 * 
	 * @param crawlDepth
	 *            the maximum crawl depth. 0 to ignore
	 */
	public void setDepth(int crawlDepth) {
		this.depth = crawlDepth;
	}

	/**
	 * @return the crawlMaximumStates
	 */
	protected int getMaximumStates() {
		return maximumStates;
	}

	/**
	 * Sets the maximum number of states. Crawljax will stop crawling when this maximum number of
	 * states are found
	 * 
	 * @param crawlMaximumStates
	 *            the maximum number of states. 0 specifies no bound for the number of crawl states.
	 */
	public void setMaximumStates(int crawlMaximumStates) {
		this.maximumStates = crawlMaximumStates;
	}

	/**
	 * @return the crawlMaximumRuntime
	 */
	protected int getMaximumRuntime() {
		return maximumRuntime;
	}

	/**
	 * Sets the maximum time for Crawljax to run. Crawljax will stop crawling when this timelimit is
	 * reached.
	 * 
	 * @param seconds
	 *            the crawlMaximumRuntime to set
	 */
	public void setMaximumRuntime(int seconds) {
		this.maximumRuntime = seconds;
	}

	/**
	 * @return whether to Crawljax should enter random values in form input fields
	 */
	protected boolean getRandomInputInForms() {
		return randomInputInForms;
	}

	/**
	 * @param value
	 *            whether to Crawljax should enter random values in form input fields
	 */
	public void setRandomInputInForms(boolean value) {
		this.randomInputInForms = value;
	}

	/**
	 * @return the number of milliseconds to wait after reloading the url
	 */
	protected int getWaitTimeAfterReloadUrl() {
		return waitTimeAfterReloadUrl;
	}

	/**
	 * @param milliseconds
	 *            the number of milliseconds to wait after reloading the url
	 */
	public void setWaitTimeAfterReloadUrl(int milliseconds) {
		this.waitTimeAfterReloadUrl = milliseconds;
	}

	/**
	 * @return the number the number of milliseconds to wait after an event is fired
	 */
	protected int getWaitTimeAfterEvent() {
		return waitTimeAfterEvent;
	}

	/**
	 * @param milliseconds
	 *            the number of milliseconds to wait after an event is fired
	 */
	public void setWaitTimeAfterEvent(int milliseconds) {
		this.waitTimeAfterEvent = milliseconds;
	}

	/**
	 * @return the events that should be fired (e.g. onclick)
	 */
	protected ImmutableList<EventType> getCrawlEvents() {
		return ImmutableList.copyOf(crawlEvents);
	}

	/**
	 * @return the inputSpecification in which data for input field is specified
	 */
	protected InputSpecification getInputSpecification() {
		return inputSpecification;
	}

	/**
	 * @param inputSpecification
	 *            in which data for input fields is specified
	 */
	public void setInputSpecification(InputSpecification inputSpecification) {
		this.inputSpecification = inputSpecification;
	}

	/**
	 * @return the different crawl actions.
	 */
	protected CrawlActions crawlActions() {
		return crawlActions;
	}

	/**
	 * @return the oracleComparators
	 */
	protected ImmutableList<OracleComparator> getOracleComparators() {
		return ImmutableList.copyOf(oracleComparators);
	}

	/**
	 * Adds the Oracle Comparator to the list of comparators.
	 * 
	 * @param id
	 *            a name for the Oracle Comparator.
	 * @param oracleComparator
	 *            the oracle to be added.
	 */
	public void addOracleComparator(String id, Comparator oracleComparator) {
		this.oracleComparators.add(new OracleComparator(id, oracleComparator));
	}

	/**
	 * Adds an Oracle Comparator with preconditions to the list of comparators.
	 * 
	 * @param id
	 *            a name for the Oracle Comparator
	 * @param oracleComparator
	 *            the oracle to be added.
	 * @param preConditions
	 *            the preconditions to be met.
	 */
	public void addOracleComparator(String id, Comparator oracleComparator,
	        Condition... preConditions) {
		this.oracleComparators.add(new OracleComparator(id, oracleComparator, preConditions));
	}

	/**
	 * @return the invariants
	 */
	protected ImmutableList<Invariant> getInvariants() {
		return ImmutableList.copyOf(invariants);
	}

	/**
	 * @param description
	 *            the description of the invariant.
	 * @param condition
	 *            the condition to be met.
	 */
	public void addInvariant(String description, Condition condition) {
		this.invariants.add(new Invariant(description, condition));
	}

	/**
	 * @param description
	 *            the description of the invariant.
	 * @param condition
	 *            the invariant condition.
	 * @param preConditions
	 *            the precondition.
	 */
	public void addInvariant(String description, Condition condition, Condition... preConditions) {
		this.invariants.add(new Invariant(description, condition, preConditions));
	}

	/**
	 * @return whether invariants should be tested while crawling.
	 */
	protected boolean getTestInvariantsWhileCrawling() {
		return testInvariantsWhileCrawling;
	}

	/**
	 * @param testInvariantsWhileCrawling
	 *            whether invariants should be tested while crawling
	 */
	public void setTestInvariantsWhileCrawling(boolean testInvariantsWhileCrawling) {
		this.testInvariantsWhileCrawling = testInvariantsWhileCrawling;
	}

	/**
	 * @return the waitConditions
	 */
	protected ImmutableList<WaitCondition> getWaitConditions() {
		return ImmutableList.copyOf(waitConditions);
	}

	/**
	 * @param url
	 *            the full url or a part of the url where should be waited for the
	 *            expectedConditions
	 * @param expectedConditions
	 *            the conditions to wait for.
	 */
	public void waitFor(String url, ExpectedCondition... expectedConditions) {
		this.waitConditions.add(new WaitCondition(url, expectedConditions));
	}

	/**
	 * @param url
	 *            the full url or a part of the url where should be waited for the
	 *            expectedConditions
	 * @param expectedConditions
	 *            the conditions to wait for
	 * @param timeout
	 *            the timeout
	 */
	public void waitFor(String url, int timeout, ExpectedCondition... expectedConditions) {
		this.waitConditions.add(new WaitCondition(url, timeout, expectedConditions));
	}

	/**
	 * @return the crawlConditions
	 */
	protected ImmutableList<CrawlCondition> getCrawlConditions() {
		return ImmutableList.copyOf(crawlConditions);
	}

	/**
	 * @param description
	 *            the description
	 * @param crawlCondition
	 *            the condition
	 */
	public void addCrawlCondition(String description, Condition crawlCondition) {
		this.crawlConditions.add(new CrawlCondition(description, crawlCondition));
	}

	/**
	 * @param description
	 *            the description
	 * @param crawlCondition
	 *            the condition
	 * @param preConditions
	 *            the preConditions
	 */
	public void addCrawlCondition(String description, Condition crawlCondition,
	        Condition... preConditions) {
		this.crawlConditions.add(new CrawlCondition(description, crawlCondition, preConditions));
	}

	/**
	 * @return the crawl once value.
	 */
	protected boolean getClickOnce() {
		return this.clicklOnce;
	}

	/**
	 * @param clickOnce
	 *            the crawl once value;
	 */
	public void setClickOnce(boolean clickOnce) {
		this.clicklOnce = clickOnce;
	}

	/**
	 * @param crawlEvents
	 *            the crawlEvents to set
	 */
	public void addCrawlEvents(List<EventType> crawlEvents) {
		this.crawlEvents.addAll(crawlEvents);
	}

	/**
	 * @param string
	 *            the frame identifier to ignore when descending into (i)frames
	 */
	public void dontCrawlFrame(String string) {
		this.ignoredFrameIdentifiers.add(string);
	}

	/**
	 * @return the list of ignored frames
	 */
	protected ImmutableList<String> ignoredFrameIdentifiers() {
		return ImmutableList.copyOf(ignoredFrameIdentifiers);
	}

	/**
	 * disable the crawling of Frames in total.
	 */
	public void disableCrawlFrames() {
		this.disableCrawlFrames = true;
	}

	/**
	 * Is the crawling of Frames enabled.
	 * 
	 * @return true if frames should be crawled false otherwise.
	 */
	protected boolean isCrawlFrames() {
		return !disableCrawlFrames;
	}
}
