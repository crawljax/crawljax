package com.crawljax.oracle;

import java.util.ArrayList;
import java.util.List;

import com.crawljax.condition.Condition;

/**
 * @author danny
 * @version $Id: ComparatorWithPreconditions.java 6382 2009-12-29 12:38:11Z danny $ This class
 *          contains the oracle and its precondition(s)
 */
public class ComparatorWithPreconditions {

	private final List<Condition> preConditions;
	private final String id;
	private final Oracle oracle;

	/**
	 * @param id
	 *            an identifier for the oracle
	 * @param oracle
	 *            the Oracle
	 */
	public ComparatorWithPreconditions(String id, Oracle oracle) {
		this(id, oracle, new ArrayList<Condition>());
	}

	/**
	 * @param id
	 *            an identifier for the oracle
	 * @param oracle
	 *            the Oracle
	 * @param preConditions
	 *            the preconditions that must be satisfied before the oracle comparator is used
	 */
	public ComparatorWithPreconditions(String id, Oracle oracle, List<Condition> preConditions) {
		this.id = id;
		this.oracle = oracle;
		this.preConditions = preConditions;
	}

	/**
	 * @param id
	 *            an identifier for the oracle
	 * @param oracle
	 *            the Oracle
	 * @param preConditions
	 *            the preconditions that must be satisfied before the oracle comparator is used
	 */
	public ComparatorWithPreconditions(String id, Oracle oracle, Condition... preConditions) {
		this.id = id;
		this.oracle = oracle;
		this.preConditions = new ArrayList<Condition>();
		for (Condition condition : preConditions) {
			this.preConditions.add(condition);
		}
	}

	/**
	 * @return the Id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the Oracle
	 */
	public Oracle getOracle() {
		return oracle;
	}

	/**
	 * @return the preconditions
	 */
	public List<Condition> getPreConditions() {
		return preConditions;
	}

}
