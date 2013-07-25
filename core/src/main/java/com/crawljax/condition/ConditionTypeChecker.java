package com.crawljax.condition;

import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Controller class for the invariants.
 */
@ThreadSafe
public class ConditionTypeChecker<T extends ConditionType> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConditionTypeChecker.class);

	private final ImmutableList<T> invariants;

	/**
	 * Constructor with invariant list.
	 * 
	 * @param invariants
	 *            The invariant list.
	 */
	public ConditionTypeChecker(ImmutableList<T> invariants) {
		Preconditions.checkNotNull(invariants);
		this.invariants = invariants;
	}

	/**
	 * @param browser
	 *            The browser.
	 * @return a list of {@link ConditionType} where {@link Condition#check(EmbeddedBrowser)}
	 *         failed.
	 */
	public ImmutableList<T> getFailedConditions(EmbeddedBrowser browser) {
		LOGGER.debug("Checking {} ConditionTypes", invariants.size());
		ImmutableList.Builder<T> builder = ImmutableList.builder();
		for (T invariant : invariants) {
			if (preConditionsHold(browser, invariant)) {
				Condition invariantCondition = invariant.getCondition();
				LOGGER.debug("Checking ConditionType: {}", invariant.getDescription());
				if (!invariantCondition.check(browser)) {
					LOGGER.debug("ConditionType '{}' failed", invariant.getDescription());
					builder.add(invariant);
				}
			}
		}
		return builder.build();
	}

	private boolean preConditionsHold(EmbeddedBrowser browser, T invariant) {
		for (Condition condition : invariant.getPreConditions()) {
			if (!condition.check(browser)) {
				LOGGER.debug("Precondition failed for ConditionType: {} - PreCondition: {} : ",
				        invariant.getDescription(), condition);
				return false;
			}
		}
		LOGGER.debug("Preconditions hold for ConditionType: {}", invariant.getDescription());
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(invariants);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof ConditionTypeChecker) {
			ConditionTypeChecker<?> that = (ConditionTypeChecker<?>) object;
			return Objects.equal(this.invariants, that.invariants);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
		        .add("invariants", invariants)
		        .toString();
	}

}
