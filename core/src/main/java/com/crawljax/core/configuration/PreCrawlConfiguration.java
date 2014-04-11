package com.crawljax.core.configuration;

import com.crawljax.condition.browserwaiter.WaitCondition;
import com.crawljax.condition.crawlcondition.CrawlCondition;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.tuple.Pair;

public class PreCrawlConfiguration {

	public static class PreCrawlConfigurationBuilder {

		private final PreCrawlConfiguration preCrawlConfiguration;

		private final ImmutableList.Builder<WaitCondition> waitConditions = ImmutableList
				.builder();
		private final ImmutableList.Builder<CrawlCondition> crawlConditions = ImmutableList
				.builder();

		private PreCrawlConfigurationBuilder() {
			preCrawlConfiguration = new PreCrawlConfiguration();
		}

		/**
		 * @param condition add a {@link WaitCondition}.
		 */
		public PreCrawlConfigurationBuilder addWaitCondition(WaitCondition... condition) {
			waitConditions.add(condition);
			return this;
		}

		/**
		 * @param condition Add a {@link CrawlCondition}.
		 */
		public PreCrawlConfigurationBuilder addCrawlCondition(CrawlCondition... condition) {
			crawlConditions.add(condition);
			return this;
		}


		PreCrawlConfiguration build(CrawlActionsBuilder crawlActionsBuilder) {
			Pair<ImmutableList<CrawlElement>, ImmutableList<CrawlElement>> elements =
					crawlActionsBuilder.build();
			preCrawlConfiguration.includedElements = elements.getLeft();
			preCrawlConfiguration.excludedElements = elements.getRight();

			preCrawlConfiguration.waitConditions = waitConditions.build();
			preCrawlConfiguration.crawlConditions = crawlConditions.build();
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


	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("waitConditions", waitConditions)
				.add("crawlConditions", crawlConditions)
				.add("includedElements", includedElements)
				.add("excludedElements", excludedElements)
				.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(waitConditions, crawlConditions, includedElements,
				excludedElements);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof PreCrawlConfiguration) {
			PreCrawlConfiguration that = (PreCrawlConfiguration) object;
			return Objects.equal(this.waitConditions, that.waitConditions)
					&& Objects.equal(this.crawlConditions, that.crawlConditions)
					&& Objects.equal(this.includedElements, that.includedElements)
					&& Objects.equal(this.excludedElements, that.excludedElements);
		}
		return false;
	}

}