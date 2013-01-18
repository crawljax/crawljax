package com.crawljax.core.configuration;

import com.crawljax.condition.browserwaiter.WaitCondition;
import com.crawljax.condition.crawlcondition.CrawlCondition;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.oraclecomparator.OracleComparator;
import com.google.common.collect.ImmutableList;

/**
 * Reader class for crawlspecification.
 */
public class CrawlSpecificationReader implements IgnoreFrameChecker {

	private final CrawlSpecification crawlSpecification;

	/**
	 * @param crawlSpecification
	 *            The specification to wrap around.
	 */
	public CrawlSpecificationReader(CrawlSpecification crawlSpecification) {
		super();
		this.crawlSpecification = crawlSpecification;
	}

	/**
	 * @return the number of milliseconds to wait after reloading the url
	 */
	public int getWaitAfterReloadUrl() {
		return this.crawlSpecification.getWaitTimeAfterReloadUrl();
	}

	/**
	 * @return the number the number of milliseconds to wait after an event is fired
	 */
	public int getWaitAfterEvent() {
		return this.crawlSpecification.getWaitTimeAfterEvent();
	}

	/**
	 * @return the oracleComparators
	 */
	public ImmutableList<OracleComparator> getOracleComparators() {
		return ImmutableList.copyOf(crawlSpecification.getOracleComparators());
	}

	/**
	 * @return the invariants
	 */
	public ImmutableList<Invariant> getInvariants() {
		return ImmutableList.copyOf(crawlSpecification.getInvariants());
	}

	/**
	 * @return the waitConditions
	 */
	public ImmutableList<WaitCondition> getWaitConditions() {
		return ImmutableList.copyOf(crawlSpecification.getWaitConditions());
	}

	/**
	 * @return the crawlConditions
	 */
	public ImmutableList<CrawlCondition> getCrawlConditions() {
		return ImmutableList.copyOf(crawlSpecification.getCrawlConditions());
	}

	/**
	 * @return the depth level.
	 */
	public int getDepth() {
		return crawlSpecification.getDepth();
	}

	/**
	 * @return the URL of the site.
	 */
	public String getSiteUrl() {
		return crawlSpecification.getUrl();
	}

	/**
	 * @return true if each candidate element should be examined only once.
	 */
	public boolean getClickOnce() {
		return crawlSpecification.getClickOnce();
	}

	/**
	 * @return the maximum crawling time.
	 */
	public int getMaximumRunTime() {
		return crawlSpecification.getMaximumRuntime();
	}

	/**
	 * @return maximum number of states to be crawled.
	 */
	public int getMaxNumberOfStates() {

		return crawlSpecification.getMaximumStates();
	}

	/**
	 * @return true if random input should be generated for input fileds.
	 */
	public boolean getRandomInputInForms() {
		return crawlSpecification.getRandomInputInForms();
	}

	@Override
	public boolean isFrameIgnored(String iFrame) {
		if (!crawlSpecification.isCrawlFrames()) {
			return true;
		}
		for (String ignorePattern : crawlSpecification.ignoredFrameIdentifiers()) {
			if (ignorePattern.contains("%")) {
				// replace with a useful wildcard for regex
				String pattern = ignorePattern.replace("%", ".*");
				if (iFrame.matches(pattern)) {
					return true;
				}
			} else {
				if (ignorePattern.equals(iFrame)) {
					return true;
				}
			}
		}
		return false;
	}
}
