package com.crawljax.core.configuration;

import java.util.concurrent.TimeUnit;

import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.configuration.PreCrawlConfiguration.PreCrawlConfigurationBuilder;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.oraclecomparator.OracleComparator;
import com.google.common.collect.ImmutableList;
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

		private CrawlRulesBuilder() {
			crawlRules = new CrawlRules();
			crawlActionsBuilder = new CrawlActionsBuilder();
			preCrawlConfig = PreCrawlConfiguration.builder();
		}

		public CrawlRulesBuilder insertRandomDataInInputForms(boolean insertRandomData) {
			crawlRules.randomInputInForms = insertRandomData;
			return this;
		}

		public CrawlActionsBuilder defineAction() {
			return crawlActionsBuilder;
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

		public CrawlRulesBuilder addOracleComparator(OracleComparator... oracle) {
			oracleComparators.add(oracle);
			return this;
		}

		public PreCrawlConfigurationBuilder preCrawlConfig() {
			return preCrawlConfig;
		}

		public CrawlRulesBuilder setInputSpec(InputSpecification spec) {
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
			crawlRules.disableCrawlFrames = !frames;
			return this;
		}

		/**
		 * @param time
		 *            The time to wait after a URL is loaded. Default is 500 milliseconds.
		 * @param unit
		 *            The time unit.
		 */
		public CrawlRulesBuilder waitAfterReloadUrl(long time, TimeUnit unit) {
			crawlRules.waitAfterReloadUrlMillis = time;
			return this;
		}

		/**
		 * @param time
		 *            The time to wait after an event is fired. Default is 500 milliseconds.
		 * @param unit
		 *            The time unit.
		 */
		public CrawlRulesBuilder waitAfterEvent(long time, TimeUnit unit) {
			crawlRules.waitAfterReloadUrlMillis = time;
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

		CrawlRules build() {
			crawlRules.crawlEvents = crawlEvents.build();
			if (crawlRules.crawlEvents.isEmpty()) {
				crawlRules.crawlEvents = ImmutableSortedSet.of(EventType.click);
			}
			crawlRules.invariants = invariants.build();
			crawlRules.oracleComparators = oracleComparators.build();
			crawlRules.preCrawlConfig = preCrawlConfig.build(crawlActionsBuilder);
			return crawlRules;
		}
	}

	public static CrawlRulesBuilder builder() {
		return new CrawlRulesBuilder();
	}

	private ImmutableSortedSet<EventType> crawlEvents;

	private ImmutableList<Invariant> invariants;
	private ImmutableList<OracleComparator> oracleComparators;

	private PreCrawlConfiguration preCrawlConfig;

	private boolean randomInputInForms = true;
	private InputSpecification inputSpecification;
	private boolean testInvariantsWhileCrawling = true;
	private boolean clickOnce = true;
	private boolean disableCrawlFrames = false;
	private boolean crawlHiddenAnchors = false;
	public long waitAfterReloadUrlMillis = 500;
	public long waitAfterEvent = 500;

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

	public boolean isDisableCrawlFrames() {
		return disableCrawlFrames;
	}

	public boolean isCrawlHiddenAnchors() {
		return crawlHiddenAnchors;
	}

	/**
	 * @return in milliseconds
	 */
	public long getWaitAfterReloadUrl() {
		return waitAfterReloadUrlMillis;
	}

	/**
	 * @return in milliseconds.
	 */
	public long getWaitAfterEvent() {
		return waitAfterEvent;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (clickOnce ? 1231 : 1237);
		result = prime * result + ((crawlEvents == null) ? 0 : crawlEvents.hashCode());
		result = prime * result + (crawlHiddenAnchors ? 1231 : 1237);
		result = prime * result + (disableCrawlFrames ? 1231 : 1237);
		result =
		        prime * result
		                + ((inputSpecification == null) ? 0 : inputSpecification.hashCode());
		result = prime * result + ((invariants == null) ? 0 : invariants.hashCode());
		result =
		        prime * result + ((oracleComparators == null) ? 0 : oracleComparators.hashCode());
		result = prime * result + ((preCrawlConfig == null) ? 0 : preCrawlConfig.hashCode());
		result = prime * result + (randomInputInForms ? 1231 : 1237);
		result = prime * result + (testInvariantsWhileCrawling ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CrawlRules other = (CrawlRules) obj;
		if (clickOnce != other.clickOnce)
			return false;
		if (crawlEvents == null) {
			if (other.crawlEvents != null)
				return false;
		} else if (!crawlEvents.equals(other.crawlEvents))
			return false;
		if (crawlHiddenAnchors != other.crawlHiddenAnchors)
			return false;
		if (disableCrawlFrames != other.disableCrawlFrames)
			return false;
		if (inputSpecification == null) {
			if (other.inputSpecification != null)
				return false;
		} else if (!inputSpecification.equals(other.inputSpecification))
			return false;
		if (invariants == null) {
			if (other.invariants != null)
				return false;
		} else if (!invariants.equals(other.invariants))
			return false;
		if (oracleComparators == null) {
			if (other.oracleComparators != null)
				return false;
		} else if (!oracleComparators.equals(other.oracleComparators))
			return false;
		if (preCrawlConfig == null) {
			if (other.preCrawlConfig != null)
				return false;
		} else if (!preCrawlConfig.equals(other.preCrawlConfig))
			return false;
		if (randomInputInForms != other.randomInputInForms)
			return false;
		if (testInvariantsWhileCrawling != other.testInvariantsWhileCrawling)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CrawlRules [crawlEvents=");
		builder.append(crawlEvents);
		builder.append(", invariants=");
		builder.append(invariants);
		builder.append(", oracleComparators=");
		builder.append(oracleComparators);
		builder.append(", preCrawlConfig=");
		builder.append(preCrawlConfig);
		builder.append(", randomInputInForms=");
		builder.append(randomInputInForms);
		builder.append(", inputSpecification=");
		builder.append(inputSpecification);
		builder.append(", testInvariantsWhileCrawling=");
		builder.append(testInvariantsWhileCrawling);
		builder.append(", clicklOnce=");
		builder.append(clickOnce);
		builder.append(", disableCrawlFrames=");
		builder.append(disableCrawlFrames);
		builder.append(", crawlHiddenAnchors=");
		builder.append(crawlHiddenAnchors);
		builder.append("]");
		return builder.toString();
	}

}
