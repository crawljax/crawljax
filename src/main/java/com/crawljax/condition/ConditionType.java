/**
 * 
 */
package com.crawljax.condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * A combination of a condition and preconditions.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
@ThreadSafe
public abstract class ConditionType {

	private final List<Condition> preConditions = new ArrayList<Condition>();
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
		this.preConditions.addAll(preConditions);
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
	@GuardedBy("preConditions")
	public synchronized List<Condition> getPreConditions() {
		synchronized (preConditions) {
			return preConditions;
		}
	}

	/**
	 * @param preCondition
	 *            the precondition.
	 */
	@GuardedBy("preConditions")
	public void addPreCondition(Condition preCondition) {
		synchronized (preConditions) {
			this.preConditions.add(preCondition);
		}
	}

}
