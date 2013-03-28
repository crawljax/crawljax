package com.crawljax.core.configuration;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.concurrent.TimeUnit;

import com.crawljax.condition.Condition;
import com.crawljax.condition.browserwaiter.WaitCondition;
import com.crawljax.condition.crawlcondition.CrawlCondition;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.configuration.CrawlActionsBuilder.ExcludeByParentBuilder;
import com.crawljax.core.configuration.PreCrawlConfiguration.PreCrawlConfigurationBuilder;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.oraclecomparator.OracleComparator;
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

		private CrawlRulesBuilder() {
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
		 * @see com.crawljax.core.configuration.CrawlActionsBuilder#click(java.lang.String[])
		 */
		public void click(String... tagNames) {
			crawlActionsBuilder.click(tagNames);
		}

		/**
		 * @see com.crawljax.core.configuration.CrawlActionsBuilder#clickDefaultElements()
		 */
		public void clickDefaultElements() {
			crawlActionsBuilder.clickDefaultElements();
		}

		/**
		 * @param tagName
		 * @return
		 * @see com.crawljax.core.configuration.CrawlActionsBuilder#dontClick(java.lang.String)
		 */
		public CrawlElement dontClick(String tagName) {
			return crawlActionsBuilder.dontClick(tagName);
		}

		/**
		 * @param tagname
		 * @return
		 * @see com.crawljax.core.configuration.CrawlActionsBuilder#dontClickChildrenOf(java.lang.String)
		 */
		public ExcludeByParentBuilder dontClickChildrenOf(String tagname) {
			return crawlActionsBuilder.dontClickChildrenOf(tagname);
		}

		CrawlRules build() {
			crawlRules.crawlEvents = crawlEvents.build();
			if (crawlRules.crawlEvents.isEmpty()) {
				crawlRules.crawlEvents = ImmutableSortedSet.of(EventType.click);
			}
			crawlRules.invariants = invariants.build();
			crawlRules.oracleComparators = oracleComparators.build();
			crawlRules.preCrawlConfig = preCrawlConfig.build(crawlActionsBuilder);
			crawlRules.ignoredFrameIdentifiers = ignoredFrameIdentifiers.build();
			return crawlRules;
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

	public static CrawlRulesBuilder builder() {
		return new CrawlRulesBuilder();
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
	private boolean crawlFrames = true;
	private boolean crawlHiddenAnchors = false;
	private long waitAfterReloadUrl = DEFAULT_WAIT_AFTER_RELOAD;
	private long waitAfterEvent = DEFAULT_WAIT_AFTER_EVENT;

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

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (clickOnce ? 1231 : 1237);
		result = prime * result + ((crawlEvents == null) ? 0 : crawlEvents.hashCode());
		result = prime * result + (crawlFrames ? 1231 : 1237);
		result = prime * result + (crawlHiddenAnchors ? 1231 : 1237);
		result =
		        prime
		                * result
		                + ((ignoredFrameIdentifiers == null) ? 0 : ignoredFrameIdentifiers
		                        .hashCode());
		result =
		        prime * result
		                + ((inputSpecification == null) ? 0 : inputSpecification.hashCode());
		result = prime * result + ((invariants == null) ? 0 : invariants.hashCode());
		result =
		        prime * result + ((oracleComparators == null) ? 0 : oracleComparators.hashCode());
		result = prime * result + ((preCrawlConfig == null) ? 0 : preCrawlConfig.hashCode());
		result = prime * result + (randomInputInForms ? 1231 : 1237);
		result = prime * result + (testInvariantsWhileCrawling ? 1231 : 1237);
		result = prime * result + (int) (waitAfterEvent ^ (waitAfterEvent >>> 32));
		result = prime * result + (int) (waitAfterReloadUrl ^ (waitAfterReloadUrl >>> 32));
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CrawlRules other = (CrawlRules) obj;
		if (clickOnce != other.clickOnce) {
			return false;
		}
		if (crawlEvents == null) {
			if (other.crawlEvents != null) {
				return false;
			}
		} else if (!crawlEvents.equals(other.crawlEvents)) {
			return false;
		}
		if (crawlFrames != other.crawlFrames) {
			return false;
		}
		if (crawlHiddenAnchors != other.crawlHiddenAnchors) {
			return false;
		}
		if (ignoredFrameIdentifiers == null) {
			if (other.ignoredFrameIdentifiers != null) {
				return false;
			}
		} else if (!ignoredFrameIdentifiers.equals(other.ignoredFrameIdentifiers)) {
			return false;
		}
		if (inputSpecification == null) {
			if (other.inputSpecification != null) {
				return false;
			}
		} else if (!inputSpecification.equals(other.inputSpecification)) {
			return false;
		}
		if (invariants == null) {
			if (other.invariants != null) {
				return false;
			}
		} else if (!invariants.equals(other.invariants)) {
			return false;
		}
		if (oracleComparators == null) {
			if (other.oracleComparators != null) {
				return false;
			}
		} else if (!oracleComparators.equals(other.oracleComparators)) {
			return false;
		}
		if (preCrawlConfig == null) {
			if (other.preCrawlConfig != null) {
				return false;
			}
		} else if (!preCrawlConfig.equals(other.preCrawlConfig)) {
			return false;
		}
		if (randomInputInForms != other.randomInputInForms) {
			return false;
		}
		if (testInvariantsWhileCrawling != other.testInvariantsWhileCrawling) {
			return false;
		}
		if (waitAfterEvent != other.waitAfterEvent) {
			return false;
		}
		if (waitAfterReloadUrl != other.waitAfterReloadUrl) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CrawlRules [crawlEvents=");
		builder.append(crawlEvents);
		builder.append(", invariants=");
		builder.append(invariants);
		builder.append(", oracleComparators=");
		builder.append(oracleComparators);
		builder.append(", ignoredFrameIdentifiers=");
		builder.append(ignoredFrameIdentifiers);
		builder.append(", preCrawlConfig=");
		builder.append(preCrawlConfig);
		builder.append(", randomInputInForms=");
		builder.append(randomInputInForms);
		builder.append(", inputSpecification=");
		builder.append(inputSpecification);
		builder.append(", testInvariantsWhileCrawling=");
		builder.append(testInvariantsWhileCrawling);
		builder.append(", clickOnce=");
		builder.append(clickOnce);
		builder.append(", crawlFrames=");
		builder.append(crawlFrames);
		builder.append(", crawlHiddenAnchors=");
		builder.append(crawlHiddenAnchors);
		builder.append(", waitAfterReloadUrlMillis=");
		builder.append(waitAfterReloadUrl);
		builder.append(", waitAfterEvent=");
		builder.append(waitAfterEvent);
		builder.append("]");
		return builder.toString();
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

}
