package com.crawljax.core.configuration;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.concurrent.TimeUnit;

import com.crawljax.condition.Condition;
import com.crawljax.condition.browserwaiter.WaitCondition;
import com.crawljax.condition.crawlcondition.CrawlCondition;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.configuration.CrawlActionsBuilder.ExcludeByParentBuilder;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.configuration.PreCrawlConfiguration.PreCrawlConfigurationBuilder;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.oraclecomparator.OracleComparator;
import com.crawljax.oraclecomparator.comparators.SimpleComparator;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSortedSet;

public class CrawlRules {

	public static final class CrawlRulesBuilder {

		private final CrawlRules crawlRules;
		private ImmutableSortedSet.Builder<EventType> crawlEvents = ImmutableSortedSet
		        .naturalOrder();
		private ImmutableList.Builder<Invariant> invariants = ImmutableList.builder();
		private ImmutableList.Builder<OracleComparator> oracleComparators = ImmutableList
		        .builder();
		private final CrawlActionsBuilder crawlActionsBuilder;
		private final PreCrawlConfigurationBuilder preCrawlConfig;
		private final ImmutableSortedSet.Builder<String> ignoredFrameIdentifiers =
		        ImmutableSortedSet.naturalOrder();
		private final CrawljaxConfigurationBuilder crawljaxBuilder;

		private CrawlRulesBuilder(CrawljaxConfigurationBuilder crawljaxBuilder) {
			this.crawljaxBuilder = crawljaxBuilder;
			crawlRules = new CrawlRules();
			crawlActionsBuilder = new CrawlActionsBuilder();
			preCrawlConfig = PreCrawlConfiguration.builder();
		}

		public CrawlRulesBuilder insertRandomDataInInputForms(boolean insertRandomData) {
			crawlRules.randomInputInForms = insertRandomData;
			return this;
		}

		/**
		 * @param frame
		 *            A frame that should be excluded from the DOM, before doing any other
		 *            operations.
		 */
		public CrawlRulesBuilder dontCrawlFrame(String frame) {
			Preconditions.checkNotNull(frame);
			ignoredFrameIdentifiers.add(frame);
			return this;
		}

		/**
		 * @param eventTypes
		 *            Add an {@link EventType} that should be triggered by Crawljax. If none are
		 *            defined, it will only use {@link EventType#click}
		 */
		public CrawlRulesBuilder addEventType(EventType... eventTypes) {
			crawlEvents.add(eventTypes);
			return this;
		}

		public CrawlRulesBuilder addInvariant(Invariant... invariant) {
			invariants.add(invariant);
			return this;
		}

		/**
		 * @param description
		 *            The invariants description.
		 * @param condition
		 *            The condition for the invariant.
		 * @see Invariant#Invariant(String, Condition)
		 */
		public CrawlRulesBuilder addInvariant(String description, Condition condition) {
			Preconditions.checkNotNull(description);
			Preconditions.checkNotNull(condition);
			invariants.add(new Invariant(description, condition));
			return this;
		}

		public CrawlRulesBuilder addOracleComparator(OracleComparator... oracle) {
			oracleComparators.add(oracle);
			return this;
		}

		/**
		 * @param condition
		 *            add a {@link WaitCondition}.
		 */
		public PreCrawlConfigurationBuilder addWaitCondition(WaitCondition... condition) {
			return preCrawlConfig.addWaitCondition(condition);
		}

		/**
		 * @param condition
		 *            add a {@link CrawlCondition}
		 */
		public PreCrawlConfigurationBuilder addCrawlCondition(CrawlCondition... condition) {
			return preCrawlConfig.addCrawlCondition(condition);
		}

		/**
		 * @see CrawlCondition#CrawlCondition(String, Condition)
		 */
		public PreCrawlConfigurationBuilder addCrawlCondition(String description,
		        Condition crawlCondition) {
			return preCrawlConfig.addCrawlCondition(new CrawlCondition(description,
			        crawlCondition));
		}

		/**
		 * @param names
		 *            add an attribute name you want to filter before parsing the dom.
		 */
		public PreCrawlConfigurationBuilder filterAttributeNames(String names) {
			return preCrawlConfig.filterAttributeNames(names);
		}

		public CrawlRulesBuilder setInputSpec(InputSpecification spec) {
			Preconditions.checkNotNull(spec);
			crawlRules.inputSpecification = spec;
			return this;
		}

		/**
		 * @param test
		 *            Test the invariants while crawling or not. Default is <code>true</code>.
		 */
		public CrawlRulesBuilder testInvariantsWhileCrawling(boolean test) {
			crawlRules.testInvariantsWhileCrawling = test;
			return this;
		}

		/**
		 * @param once
		 *            Set the crawler to interact with any element only once. Default is
		 *            <code>true</code>
		 */
		public CrawlRulesBuilder clickOnce(boolean once) {
			crawlRules.clickOnce = once;
			return this;
		}

		/**
		 * @param randomize
		 *            Click candidate elements derived from the DOM in random order in stead of in
		 *            the order that they are found.
		 */
		public CrawlRulesBuilder clickElementsInRandomOrder(boolean randomize) {
			crawlRules.randomizeCandidateElements = randomize;
			return this;
		}

		/**
		 * @param frames
		 *            Crawl frames in a page. Default is <code>true</code>.
		 */
		public CrawlRulesBuilder crawlFrames(boolean frames) {
			crawlRules.crawlFrames = frames;
			return this;
		}

		/**
		 * @param time
		 *            The time to wait after a URL is loaded. Default is 500 milliseconds.
		 * @param unit
		 *            The time unit.
		 */
		public CrawlRulesBuilder waitAfterReloadUrl(long time, TimeUnit unit) {
			checkArgument(time > 0, "Wait after reload time should be larget than 0");
			crawlRules.waitAfterReloadUrl = unit.toMillis(time);
			return this;
		}

		/**
		 * @param time
		 *            The time to wait after an event is fired. Default is 500 milliseconds.
		 * @param unit
		 *            The time unit.
		 */
		public CrawlRulesBuilder waitAfterEvent(long time, TimeUnit unit) {
			checkArgument(time > 0, "Wait after event time should be larget than 0");
			crawlRules.waitAfterEvent = unit.toMillis(time);
			return this;
		}

		/**
		 * Set Crawljax to click hidden anchors or not. Default is <code>false</code>.
		 * <dl>
		 * <dd>Pro:</dd>
		 * <dt>The benefit of clicking hidden anchors is that Crawljax isn't capable of clicking
		 * elements that are hidden for example because you have to hover another element first.
		 * This happens in most fold-out menus for example. Enabling this function allows Crawljax
		 * to find more states that are hidden this way.</dt>
		 * <dd>Con:</dd>
		 * <dt>If a anchor tag is never visible in the browser in any way, Crawljax will crawl it
		 * anyway. This makes the Crawl inconsistent with what the user experiences.</dt>
		 * </dl>
		 */
		public CrawlRulesBuilder crawlHiddenAnchors(boolean anchors) {
			crawlRules.crawlHiddenAnchors = anchors;
			return this;
		}

		/**
		 * @param tagName
		 * @see com.crawljax.core.configuration.CrawlActionsBuilder#click(java.lang.String)
		 */
		public CrawlElement click(String tagName) {
			return crawlActionsBuilder.click(tagName);
		}

		/**
		 * @param tagNames
		 * @return
		 * @see com.crawljax.core.configuration.CrawlActionsBuilder#click(java.lang.String[])
		 */
		public CrawlRulesBuilder click(String... tagNames) {
			crawlActionsBuilder.click(tagNames);
			return this;
		}

		/**
		 * @return
		 * @see com.crawljax.core.configuration.CrawlActionsBuilder#clickDefaultElements()
		 */
		public CrawlRulesBuilder clickDefaultElements() {
			crawlActionsBuilder.clickDefaultElements();
			return this;
		}

		/**
		 * @see com.crawljax.core.configuration.CrawlActionsBuilder#dontClick(java.lang.String)
		 */
		public CrawlElement dontClick(String tagName) {
			return crawlActionsBuilder.dontClick(tagName);
		}

		/**
		 * @param tagname
		 * @see com.crawljax.core.configuration.CrawlActionsBuilder#dontClickChildrenOf(java.lang.String)
		 */
		public ExcludeByParentBuilder dontClickChildrenOf(String tagname) {
			return crawlActionsBuilder.dontClickChildrenOf(tagname);
		}

		/**
		 * Follow links in anchor tags that have an <code>href</code> element that points to an URL
		 * outside of this website. This does not prevent JavaScript from opening an external URL.
		 * <p>
		 * Once the browser reaches an external URL it will <em>not</em> accept that URL as a new
		 * state. Crawljax is not meant to crawl outside a website. This option exists so that you
		 * can check all URLs for <code>404</code> errors.
		 * 
		 * @param follow
		 *            Set to true to follow exteranl urls. Default is <code>false</code>.
		 */
		public CrawlRulesBuilder followExternalLinks(boolean follow) {
			crawlRules.followExternalLinks = follow;
			return this;
		}

		/**
		 * Helper method for method chaining. Now you can do
		 * 
		 * <pre>
		 * CrawljaxConfiguration.builderFor(&quot;http://example.com&quot;)
		 *         .crawlRules()
		 *         .followExternalLinks(true)
		 *         .endRules()
		 *         .build();
		 * </pre>
		 * 
		 * @return The {@link CrawljaxConfigurationBuilder} to make method chaining easier.
		 */
		public CrawljaxConfigurationBuilder endRules() {
			return crawljaxBuilder;
		}

		CrawlRules build() {
			crawlRules.crawlEvents = crawlEvents.build();
			if (crawlRules.crawlEvents.isEmpty()) {
				crawlRules.crawlEvents = ImmutableSortedSet.of(EventType.click);
			}
			crawlRules.invariants = invariants.build();
			setupOracleComparatorsOrDefault();
			crawlRules.preCrawlConfig = preCrawlConfig.build(crawlActionsBuilder);
			crawlRules.ignoredFrameIdentifiers = ignoredFrameIdentifiers.build();
			return crawlRules;
		}

		private void setupOracleComparatorsOrDefault() {
			ImmutableList<OracleComparator> comparators = oracleComparators.build();
			if (comparators.isEmpty()) {
				crawlRules.oracleComparators =
				        ImmutableList.of(new OracleComparator("SimpleComparator",
				                new SimpleComparator()));
			} else {
				crawlRules.oracleComparators = comparators;
			}
		}
	}

	/**
	 * Default wait after URL reload in {@link TimeUnit#MILLISECONDS}
	 */
	public static final long DEFAULT_WAIT_AFTER_RELOAD = 500;

	/**
	 * Default wait after event in {@link TimeUnit#MILLISECONDS}
	 */
	public static final long DEFAULT_WAIT_AFTER_EVENT = 500;

	public static CrawlRulesBuilder builder(CrawljaxConfigurationBuilder builder) {
		return new CrawlRulesBuilder(builder);
	}

	private ImmutableSortedSet<EventType> crawlEvents;

	private ImmutableList<Invariant> invariants;
	private ImmutableList<OracleComparator> oracleComparators;
	private ImmutableSortedSet<String> ignoredFrameIdentifiers;

	private PreCrawlConfiguration preCrawlConfig;

	private boolean randomInputInForms = true;
	private InputSpecification inputSpecification = new InputSpecification();
	private boolean testInvariantsWhileCrawling = true;
	private boolean clickOnce = true;
	private boolean randomizeCandidateElements = false;
	private boolean crawlFrames = true;
	private boolean crawlHiddenAnchors = false;
	private long waitAfterReloadUrl = DEFAULT_WAIT_AFTER_RELOAD;
	private long waitAfterEvent = DEFAULT_WAIT_AFTER_EVENT;
	private boolean followExternalLinks = false;

	private CrawlRules() {
	}

	public ImmutableSortedSet<EventType> getCrawlEvents() {
		return crawlEvents;
	}

	public ImmutableList<Invariant> getInvariants() {
		return invariants;
	}

	public ImmutableList<OracleComparator> getOracleComparators() {
		return oracleComparators;
	}

	public PreCrawlConfiguration getPreCrawlConfig() {
		return preCrawlConfig;
	}

	public boolean isRandomInputInForms() {
		return randomInputInForms;
	}

	public InputSpecification getInputSpecification() {
		return inputSpecification;
	}

	public boolean isTestInvariantsWhileCrawling() {
		return testInvariantsWhileCrawling;
	}

	public boolean isClickOnce() {
		return clickOnce;
	}

	public boolean isRandomizeCandidateElements() {
		return randomizeCandidateElements;
	}

	public boolean shouldCrawlFrames() {
		return crawlFrames;
	}

	public boolean isCrawlHiddenAnchors() {
		return crawlHiddenAnchors;
	}

	/**
	 * @return in milliseconds
	 */
	public long getWaitAfterReloadUrl() {
		return waitAfterReloadUrl;
	}

	/**
	 * @return in milliseconds.
	 */
	public long getWaitAfterEvent() {
		return waitAfterEvent;
	}

	public ImmutableSortedSet<String> getIgnoredFrameIdentifiers() {
		return ignoredFrameIdentifiers;
	}

	/**
	 * @return All Crawl elements: {@link PreCrawlConfiguration#getIncludedElements()},
	 *         {@link PreCrawlConfiguration#getExcludedElements()} and
	 *         {@link InputSpecification#getCrawlElements()}.
	 */
	public ImmutableList<CrawlElement> getAllCrawlElements() {
		return new Builder<CrawlElement>()
		        .addAll(getPreCrawlConfig().getIncludedElements())
		        .addAll(getPreCrawlConfig().getExcludedElements())
		        .addAll(getInputSpecification().getCrawlElements()).build();

	}

	public boolean followExternalLinks() {
		return followExternalLinks;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(crawlEvents, invariants, oracleComparators,
		        ignoredFrameIdentifiers, preCrawlConfig, randomInputInForms, inputSpecification,
		        testInvariantsWhileCrawling, clickOnce, crawlFrames, crawlHiddenAnchors,
		        waitAfterReloadUrl, waitAfterEvent, followExternalLinks);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof CrawlRules) {
			CrawlRules that = (CrawlRules) object;
			return Objects.equal(this.crawlEvents, that.crawlEvents)
			        && Objects.equal(this.invariants, that.invariants)
			        && Objects.equal(this.oracleComparators, that.oracleComparators)
			        && Objects.equal(this.ignoredFrameIdentifiers, that.ignoredFrameIdentifiers)
			        && Objects.equal(this.preCrawlConfig, that.preCrawlConfig)
			        && Objects.equal(this.randomInputInForms, that.randomInputInForms)
			        && Objects.equal(this.inputSpecification, that.inputSpecification)
			        && Objects.equal(this.testInvariantsWhileCrawling,
			                that.testInvariantsWhileCrawling)
			        && Objects.equal(this.clickOnce, that.clickOnce)
			        && Objects.equal(this.randomizeCandidateElements,
			                that.randomizeCandidateElements)
			        && Objects.equal(this.crawlFrames, that.crawlFrames)
			        && Objects.equal(this.crawlHiddenAnchors, that.crawlHiddenAnchors)
			        && Objects.equal(this.waitAfterReloadUrl, that.waitAfterReloadUrl)
			        && Objects.equal(this.waitAfterEvent, that.waitAfterEvent)
			        && Objects.equal(this.followExternalLinks, that.followExternalLinks);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
		        .add("DEFAULT_WAIT_AFTER_RELOAD", DEFAULT_WAIT_AFTER_RELOAD)
		        .add("DEFAULT_WAIT_AFTER_EVENT", DEFAULT_WAIT_AFTER_EVENT)
		        .add("crawlEvents", crawlEvents)
		        .add("invariants", invariants)
		        .add("oracleComparators", oracleComparators)
		        .add("ignoredFrameIdentifiers", ignoredFrameIdentifiers)
		        .add("preCrawlConfig", preCrawlConfig)
		        .add("randomInputInForms", randomInputInForms)
		        .add("inputSpecification", inputSpecification)
		        .add("testInvariantsWhileCrawling", testInvariantsWhileCrawling)
		        .add("clickOnce", clickOnce)
		        .add("randomizeCandidateElements", randomizeCandidateElements)
		        .add("crawlFrames", crawlFrames)
		        .add("crawlHiddenAnchors", crawlHiddenAnchors)
		        .add("waitAfterReloadUrl", waitAfterReloadUrl)
		        .add("waitAfterEvent", waitAfterEvent)
		        .add("followExternalLinks", followExternalLinks)
		        .toString();
	}

}
