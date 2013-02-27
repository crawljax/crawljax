package com.crawljax.core.configuration;

import org.apache.commons.lang3.tuple.Pair;

import com.crawljax.condition.browserwaiter.WaitCondition;
import com.crawljax.condition.crawlcondition.CrawlCondition;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;

public class PreCrawlConfiguration {

	public static class PreCrawlConfigurationBuilder {
		private final ImmutableSortedSet.Builder<String> ignoredFrameIdentifiers =
		        ImmutableSortedSet.naturalOrder();

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

		/**
		 * @param frame
		 *            A frame that should be excluded from the DOM, before doing any other
		 *            operations.
		 */
		public PreCrawlConfigurationBuilder ignoreFrame(String frame) {
			ignoredFrameIdentifiers.add(frame);
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
			preCrawlConfiguration.ignoredFrameIdentifiers = ignoredFrameIdentifiers.build();
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
	private ImmutableSortedSet<String> ignoredFrameIdentifiers;

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

	public ImmutableSortedSet<String> getIgnoredFrameIdentifiers() {
		return ignoredFrameIdentifiers;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PreCrawlConfiguration [waitConditions=");
		builder.append(waitConditions);
		builder.append(", crawlConditions=");
		builder.append(crawlConditions);
		builder.append(", includedElements=");
		builder.append(includedElements);
		builder.append(", excludedElements=");
		builder.append(excludedElements);
		builder.append(", filterAttributeNames=");
		builder.append(filterAttributeNames);
		builder.append(", ignoredFrameIdentifiers=");
		builder.append(ignoredFrameIdentifiers);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((crawlConditions == null) ? 0 : crawlConditions.hashCode());
		result = prime * result + ((excludedElements == null) ? 0 : excludedElements.hashCode());
		result =
		        prime * result
		                + ((filterAttributeNames == null) ? 0 : filterAttributeNames.hashCode());
		result =
		        prime
		                * result
		                + ((ignoredFrameIdentifiers == null) ? 0 : ignoredFrameIdentifiers
		                        .hashCode());
		result = prime * result + ((includedElements == null) ? 0 : includedElements.hashCode());
		result = prime * result + ((waitConditions == null) ? 0 : waitConditions.hashCode());
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
		PreCrawlConfiguration other = (PreCrawlConfiguration) obj;
		if (crawlConditions == null) {
			if (other.crawlConditions != null)
				return false;
		} else if (!crawlConditions.equals(other.crawlConditions))
			return false;
		if (excludedElements == null) {
			if (other.excludedElements != null)
				return false;
		} else if (!excludedElements.equals(other.excludedElements))
			return false;
		if (filterAttributeNames == null) {
			if (other.filterAttributeNames != null)
				return false;
		} else if (!filterAttributeNames.equals(other.filterAttributeNames))
			return false;
		if (ignoredFrameIdentifiers == null) {
			if (other.ignoredFrameIdentifiers != null)
				return false;
		} else if (!ignoredFrameIdentifiers.equals(other.ignoredFrameIdentifiers))
			return false;
		if (includedElements == null) {
			if (other.includedElements != null)
				return false;
		} else if (!includedElements.equals(other.includedElements))
			return false;
		if (waitConditions == null) {
			if (other.waitConditions != null)
				return false;
		} else if (!waitConditions.equals(other.waitConditions))
			return false;
		return true;
	}

}