package com.crawljax.condition;

import java.util.Arrays;
import java.util.List;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

/**
 * A combination of a condition and preconditions.
 */
@Immutable
public abstract class ConditionType {

	private final ImmutableList<Condition> preConditions;
	private final String description;
	private final Condition condition;

	/**
	 * @param description
	 *            the textual description.
	 * @param condition
	 *            the actual condition.
	 */
	public ConditionType(String description, Condition condition) {
		this(description, condition, new Condition[] {});
	}

	/**
	 * @param description
	 *            the textual description.
	 * @param condition
	 *            the actual condition.
	 * @param preConditions
	 *            the preconditions.
	 */
	public ConditionType(String description, Condition condition, Condition... preConditions) {
		this(description, condition, Arrays.asList(preConditions));
	}

	/**
	 * @param description
	 *            the textual description.
	 * @param condition
	 *            the actual condition.
	 * @param preConditions
	 *            a list of preconditions.
	 */
	public ConditionType(String description, Condition condition, List<Condition> preConditions) {
		this.description = description;
		this.condition = condition;
		this.preConditions = ImmutableList.copyOf(preConditions);
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the conditionType
	 */
	public Condition getCondition() {
		return condition;
	}

	/**
	 * @return the preconditions
	 */
	public ImmutableList<Condition> getPreConditions() {
		return preConditions;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(preConditions, description, condition);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof ConditionType) {
			ConditionType that = (ConditionType) object;
			return Objects.equal(this.preConditions, that.preConditions)
			        && Objects.equal(this.description, that.description)
			        && Objects.equal(this.condition, that.condition);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
		        .add("preConditions", preConditions)
		        .add("description", description)
		        .add("condition", condition)
		        .toString();
	}

}
