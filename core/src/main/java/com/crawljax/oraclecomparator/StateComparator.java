package com.crawljax.oraclecomparator;

import javax.inject.Inject;

import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.Condition;
import com.crawljax.core.configuration.CrawlRules;
import com.google.common.collect.ImmutableList;

/**
 * Defines an Oracle Comparator which used multiple Oracles to decide whether two states are
 * equivalent.
 */
@ThreadSafe
public class StateComparator {

	private static final Logger LOGGER = LoggerFactory.getLogger(StateComparator.class.getName());

	/**
	 * This is an shared public static, as it is final and a primary type no harm can be done. Only
	 * accessed in {@link AbstractComparator#getDifferences(String, String)}.
	 */
	public static final boolean COMPARE_IGNORE_CASE = true;

	private final ImmutableList<OracleComparator> oracleComparator;

	@Inject
	public StateComparator(CrawlRules config) {
		oracleComparator = config.getOracleComparators();
	}

	/**
	 * @param browser
	 *            the current browser instance
	 * @return the stripped dom using {@link OracleComparator}s.
	 */
	public String getStrippedDom(EmbeddedBrowser browser) {
		String newDom = browser.getStrippedDom();
		for (OracleComparator oraclePreCondition : oracleComparator) {
			// use oracle if preconditions succeeds
			if (allPreConditionsSucceed(oraclePreCondition, browser)) {

				Comparator oracle = oraclePreCondition.getOracle();
				LOGGER.debug("Using {} : {}", oracle.getClass().getSimpleName(),
				        oraclePreCondition.getId());

				// TODO dodgy code. Is the equivalence check necessary?
				boolean equivalent = oracle.isEquivalent("", newDom);
				newDom = oracle.normalize(newDom);

				if (equivalent) {
					return newDom;
				}
			}
		}
		return newDom;
	}

	private boolean allPreConditionsSucceed(OracleComparator oraclePreCondition,
	        EmbeddedBrowser browser) {
		for (Condition preCondition : oraclePreCondition.getPreConditions()) {
			LOGGER.debug("Check precondition: " + preCondition.toString());
			if (!preCondition.check(browser)) {
				return false;
			}
		}
		return true;
	}

}
