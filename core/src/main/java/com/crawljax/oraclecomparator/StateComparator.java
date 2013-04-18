package com.crawljax.oraclecomparator;

import javax.inject.Inject;

import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.Condition;
import com.crawljax.core.configuration.CrawlRules;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.google.common.collect.ImmutableList;

/**
 * Defines an Oracle Comparator which used multiple Oracles to decide whether two states are
 * equivalent.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 */
@ThreadSafe
public class StateComparator {

	private static final Logger LOGGER = LoggerFactory.getLogger(StateComparator.class.getName());

	/**
	 * This is an shared public static, as it is final and a primary type no harm can be done. Only
	 * accessed in {@link AbstractComparator#compare()}.
	 */
	public static final boolean COMPARE_IGNORE_CASE = true;

	private final ImmutableList<OracleComparator> oracleComparator;

	/**
	 * @param comparatorsWithPreconditions
	 *            comparators with one or more preconditions
	 */
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
		String newDom = browser.getDom();
		for (OracleComparator oraclePreCondition : oracleComparator) {
			// use oracle if preconditions succeeds
			if (allPreConditionsSucceed(oraclePreCondition, browser)) {

				Comparator oracle = oraclePreCondition.getOracle();
				LOGGER.debug("Using {} : {}", oracle.getClass().getSimpleName(),
				        oraclePreCondition.getId());

				boolean equivalent = false;
				// Synchronise on a single oracle setting the doms at first and later retrieve them
				// after synchronised processing.
				// TODO Stefan the ultimate Goal is to remove this synchronisation
				synchronized (oracle) {
					oracle.setOriginalDom("");
					oracle.setNewDom(newDom);

					equivalent = oracle.isEquivalent();

					newDom = oracle.getNewDom();
				}

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
