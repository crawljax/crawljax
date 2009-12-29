package com.crawljax.oracle;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.Condition;
import com.crawljax.oracle.oracles.SimpleOracle;

/**
 * Defines an Oracle Comparator which used multiple Oracles to decide whether two states are
 * equivalent.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $id$
 */
public class OracleComparator {

	private static final Logger LOGGER = Logger.getLogger(OracleComparator.class.getName());

	public static final boolean COMPARE_IGNORE_CASE = true;
	private List<ComparatorWithPreconditions> comparatorsWithPreconditions =
	        new ArrayList<ComparatorWithPreconditions>();

	private String originalDom;
	private String newDom;

	private String strippedOriginalDom;
	private String strippedNewDom;

	private static List<ComparatorWithPreconditions> lastUsedOraclePreConditions =
	        new ArrayList<ComparatorWithPreconditions>();

	/**
	 * @param comparatorsWithPreconditions
	 *            comparators with one or more preconditions
	 */
	public OracleComparator(List<ComparatorWithPreconditions> comparatorsWithPreconditions) {
		this.comparatorsWithPreconditions = comparatorsWithPreconditions;
	}

	/**
	 * @param comparatorsWithPreconditions
	 *            the comparatorsWithPreconditions to set
	 */
	public void setOraclePreConditions(
	        List<ComparatorWithPreconditions> comparatorsWithPreconditions) {
		if (comparatorsWithPreconditions != null) {
			this.comparatorsWithPreconditions = comparatorsWithPreconditions;
		}

		// always end with SimpleOracle to remove newline differences which
		// could be caused by other oracles
		this.comparatorsWithPreconditions.add(new ComparatorWithPreconditions("SimpleComparator",
		        new SimpleOracle()));
	}

	/**
	 * @param originalDom
	 *            the original dom
	 * @param newDom
	 *            the current DOM in the browser
	 * @param browser
	 *            the current browser instance
	 * @return true iff originalDom and newDom are equivalent. Determining equivalence is done with
	 *         oracles and pre-conditions.
	 */
	public boolean compare(String originalDom, String newDom, EmbeddedBrowser browser) {

		this.originalDom = originalDom;
		this.newDom = newDom;
		this.strippedOriginalDom = originalDom;
		this.strippedNewDom = newDom;
		if (comparatorsWithPreconditions.size() == 0) {
			// add default simpleOracle
			setOraclePreConditions(null);
		}
		// System.out.println("Comparing: " + comparatorsWithPreconditions);
		for (ComparatorWithPreconditions oraclePreCondition : comparatorsWithPreconditions) {

			// checking the preconditions
			boolean preConditionsSucceed = true;

			for (Condition preCondition : oraclePreCondition.getPreConditions()) {
				boolean check = preCondition.check(browser);
				LOGGER.debug("Check precondition: " + preCondition.toString() + ": " + check);
				if (!check) {
					preConditionsSucceed = false;
					break;
				}
			}

			// use oracle if precondition succeeds
			if (preConditionsSucceed) {

				Oracle oracle = oraclePreCondition.getOracle();
				LOGGER.debug("Using " + oracle.getClass().getSimpleName() + ": "
				        + oraclePreCondition.getId());
				lastUsedOraclePreConditions.add(oraclePreCondition);
				oracle.setOriginalDom(getStrippedOriginalDom());
				oracle.setNewDom(getStrippedNewDom());

				boolean equivalent = oracle.isEquivalent();
				strippedOriginalDom = oracle.getOriginalDom();
				strippedNewDom = oracle.getNewDom();

				if (equivalent) {
					return true;
				}

			}
		}

		return false;
	}

	/**
	 * @param browser
	 *            the current browser instance
	 * @return the stripped fom by the oracle comparators
	 */
	public String getStrippedDom(EmbeddedBrowser browser) {
		OracleComparator oc = new OracleComparator(comparatorsWithPreconditions);
		try {
			oc.compare("", browser.getDom(), browser);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return oc.getStrippedNewDom();
	}

	/**
	 * @return the originalDom
	 */
	public String getOriginalDom() {
		return originalDom;
	}

	/**
	 * @return the newDom
	 */
	public String getNewDom() {
		return newDom;
	}

	/**
	 * @return the strippedOriginalDom
	 */
	public String getStrippedOriginalDom() {
		return strippedOriginalDom;
	}

	/**
	 * @return the strippedNewDom
	 */
	public String getStrippedNewDom() {
		return strippedNewDom;
	}

	/**
	 * @return the lastUsedOracles
	 */
	public List<ComparatorWithPreconditions> getLastUsedOraclePreConditions() {
		return lastUsedOraclePreConditions;
	}

	/**
	 * @return the oraclePreConditions
	 */
	public List<ComparatorWithPreconditions> getOraclePreConditions() {
		return comparatorsWithPreconditions;
	}

}
