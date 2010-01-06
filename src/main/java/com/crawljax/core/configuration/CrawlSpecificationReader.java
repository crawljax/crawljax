package com.crawljax.core.configuration;

import java.util.List;

import com.crawljax.condition.browserwaiter.WaitCondition;
import com.crawljax.condition.crawlcondition.CrawlCondition;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.oraclecomparator.OracleComparator;


/**
 * Reader class for crawlspecification. For internal use only
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id: CrawlSpecificationReader.java 6234 2009-12-18 13:46:37Z mesbah $
 */
public class CrawlSpecificationReader {

	private final CrawlSpecification crawlSpecification;

	/**
	 * 
	 * @param crawlSpecification The specification to wrap around.
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
	 * @return the number the number of milliseconds to wait after an event is
	 *         fired
	 */
	public int getWaitAfterEvent() {
		return this.crawlSpecification.getWaitTimeAfterEvent();
	}

	/**
	 * @return the oracleComparators
	 */
	public List<OracleComparator> getOracleComparators() {
		return crawlSpecification.getOracleComparators();
	}

	/**
	 * @return the invariants
	 */
	public List<Invariant> getInvariants() {
		return crawlSpecification.getInvariants();
	}

	/**
	 * @return the waitConditions
	 */
	public List<WaitCondition> getWaitConditions() {
		return crawlSpecification.getWaitConditions();
	}

	/**
	 * @return the crawlConditions
	 */
	public List<CrawlCondition> getCrawlConditions() {
		return crawlSpecification.getCrawlConditions();
	}

}
