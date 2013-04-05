package com.crawljax.core.configuration;

import org.apache.commons.lang3.tuple.Pair;

import com.crawljax.condition.browserwaiter.WaitCondition;
import com.crawljax.condition.crawlcondition.CrawlCondition;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;

public class PreCrawlConfiguration {

	public static class PreCrawlConfigurationBuilder {

		private final PreCrawlConfiguration preCrawlConfiguration;

		private final ImmutableList.Builder<WaitCondition> waitConditions = ImmutableList
		        .builder();
		private final ImmutableList.Builder<CrawlCondition> crawlConditions = ImmutableList
		        .builder();
		private final ImmutableSortedSet.Builder<String> filterAttributeNames =
		        ImmutableSortedSet.naturalOrder();

		private PreCrawlConfigurationBuilder() {
			preCrawlConfiguration = new PreCrawlConfiguration();
		}

		/**
		 * @param condition
		 *            add a {@link WaitCondition}.
		 */
		public PreCrawlConfigurationBuilder addWaitCondition(WaitCondition... condition) {
			waitConditions.add(condition);
			return this;
		}

		/**
		 * @param condition
		 *            Add a {@link CrawlCondition}.
		 */
		public PreCrawlConfigurationBuilder addCrawlCondition(CrawlCondition... condition) {
			crawlConditions.add(condition);
			return this;
		}

		/**
		 * @param names
		 *            Names of attributes that should be ignored. Default is
		 *            <code>"closure_hashcode_(\\w)*"</code> and <code>"jquery[0-9]+"</code> if none
		 *            are specified.
		 */
		public PreCrawlConfigurationBuilder filterAttributeNames(String names) {
			filterAttributeNames.add(names);
			return this;
		}

		PreCrawlConfiguration build(CrawlActionsBuilder crawlActionsBuilder) {
			Pair<ImmutableList<CrawlElement>, ImmutableList<CrawlElement>> elements =
			        crawlActionsBuilder.build();
			preCrawlConfiguration.includedElements = elements.getLeft();
			preCrawlConfiguration.excludedElements = elements.getRight();

			preCrawlConfiguration.waitConditions = waitConditions.build();
			preCrawlConfiguration.crawlConditions = crawlConditions.build();
			preCrawlConfiguration.filterAttributeNames = filterAttributeNames.build();
			if (preCrawlConfiguration.filterAttributeNames.isEmpty()) {
				preCrawlConfiguration.filterAttributeNames =
				        ImmutableSortedSet.of("closure_hashcode_(\\w)*", "jquery[0-9]+");
			}
			return preCrawlConfiguration;
		}
	}

	public static PreCrawlConfigurationBuilder builder() {
		return new PreCrawlConfigurationBuilder();
	}

	private ImmutableList<WaitCondition> waitConditions;
	private ImmutableList<CrawlCondition> crawlConditions;
	private ImmutableList<CrawlElement> includedElements;
	private ImmutableList<CrawlElement> excludedElements;
	private ImmutableSortedSet<String> filterAttributeNames;

	private PreCrawlConfiguration() {

	}

	public ImmutableList<WaitCondition> getWaitConditions() {
		return waitConditions;
	}

	public ImmutableList<CrawlCondition> getCrawlConditions() {
		return crawlConditions;
	}

	public ImmutableList<CrawlElement> getIncludedElements() {
		return includedElements;
	}

	public ImmutableList<CrawlElement> getExcludedElements() {
		return excludedElements;
	}

	public ImmutableSortedSet<String> getFilterAttributeNames() {
		return filterAttributeNames;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
		        .add("waitConditions", waitConditions)
		        .add("crawlConditions", crawlConditions)
		        .add("includedElements", includedElements)
		        .add("excludedElements", excludedElements)
		        .add("filterAttributeNames", filterAttributeNames)
		        .toString();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(waitConditions, crawlConditions, includedElements,
		        excludedElements, filterAttributeNames);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof PreCrawlConfiguration) {
			PreCrawlConfiguration that = (PreCrawlConfiguration) object;
			return Objects.equal(this.waitConditions, that.waitConditions)
			        && Objects.equal(this.crawlConditions, that.crawlConditions)
			        && Objects.equal(this.includedElements, that.includedElements)
			        && Objects.equal(this.excludedElements, that.excludedElements)
			        && Objects.equal(this.filterAttributeNames, that.filterAttributeNames);
		}
		return false;
	}

}