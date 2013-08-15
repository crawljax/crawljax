package com.crawljax.condition.invariant;

import java.util.List;

import com.crawljax.condition.Condition;
import com.crawljax.condition.ConditionType;
import com.google.common.base.Objects;

/**
 * An Invariant is an condition which should always hold when its preconditions are satisfied.
 */
public class Invariant extends ConditionType {

	/**
	 * @param description
	 *            Description of the invariant.
	 * @param invariantCondition
	 *            Condition to check.
	 */
	public Invariant(String description, Condition invariantCondition) {
		super(description, invariantCondition);
	}

	/**
	 * @param description
	 *            Description of the invariant.
	 * @param invariantCondition
	 *            Condition to check.
	 * @param preConditions
	 *            Only check if the preconditions are true.
	 */
	public Invariant(String description, Condition invariantCondition, Condition... preConditions) {
		super(description, invariantCondition, preConditions);
	}

	/**
	 * @param description
	 *            Description of the invariant.
	 * @param invariantCondition
	 *            Condition to check.
	 * @param preConditions
	 *            Only check if the preconditions are true.
	 */
	public Invariant(String description, Condition invariantCondition,
	        List<Condition> preConditions) {
		super(description, invariantCondition, preConditions);
	}

	/**
	 * @return The condition
	 */
	public Condition getInvariantCondition() {
		return getCondition();
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
		        .add("description", getDescription())
		        .add("condition", getInvariantCondition())
		        .toString();
	}

}
