/**
 * 
 */
package com.crawljax.condition.invariant;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.Condition;

/**
 * Controller class for the invariants.
 * 
 * @author danny
 * @version $Id$
 */
public class InvariantChecker {

	private static final Logger LOGGER = LoggerFactory
	        .getLogger(InvariantChecker.class.getName());

	private List<Invariant> invariants;

	/**
	 * Constructor with invariant list.
	 * 
	 * @param invariants
	 *            The invariant list.
	 */
	public InvariantChecker(List<Invariant> invariants) {
		this.invariants = invariants;
	}

	private final List<Invariant> failedInvariants = new ArrayList<Invariant>();

	/**
	 * @param browser
	 *            The browser.
	 * @return true iff browser satisfies ALL the invariants
	 */
	public boolean check(EmbeddedBrowser browser) {
		failedInvariants.clear();
		if (invariants != null) {
			LOGGER.info("Checking " + invariants.size() + " invariants");
			for (Invariant invariant : invariants) {
				boolean conditionsSucceed = true;
				for (Condition condition : invariant.getPreConditions()) {
					boolean check;
					check = condition.check(browser);
					LOGGER.debug("Checking Invariant: " + invariant.getDescription()
					        + " - PreCondition: " + condition.toString() + ": " + check);
					if (!check) {
						conditionsSucceed = false;
						break;
					}
				}
				if (conditionsSucceed) {
					Condition invariantCondition = invariant.getCondition();
					LOGGER.debug("Checking Invariant: " + invariant.getDescription());
					if (!invariantCondition.check(browser)) {
						LOGGER.debug("Invariant '" + invariant.getDescription() + "' failed: "
						        + invariant.getDescription());
						failedInvariants.add(invariant);
					}
				}
			}
		}
		if (failedInvariants.size() > 0) {
			return false;
		}
		return true;
	}

	/**
	 * @return The failedInvariants.
	 */
	public List<Invariant> getFailedInvariants() {
		return failedInvariants;
	}

	/**
	 * @return the invariants
	 */
	public List<Invariant> getInvariants() {
		return invariants;
	}

	/**
	 * @param invariants
	 *            the invariants to set
	 */
	public void setInvariants(List<Invariant> invariants) {
		this.invariants = invariants;
	}

}
