package com.crawljax.oraclecomparator;

import java.util.ArrayList;
import java.util.List;

import net.jcip.annotations.Immutable;

import com.crawljax.condition.Condition;

/**
 * This class contains the oracle and its precondition(s).
 */
@Immutable
public class OracleComparator {

	private final List<Condition> preConditions = new ArrayList<Condition>();
	private final String id;
	private final Comparator oracle;

	/**
	 * @param id
	 *            an identifier for the oracle
	 * @param oracle
	 *            the Oracle
	 */
	public OracleComparator(String id, Comparator oracle) {
		this.id = id;
		this.oracle = oracle;
	}

	/**
	 * @param id
	 *            an identifier for the oracle
	 * @param oracle
	 *            the Oracle
	 * @param preConditions
	 *            the preconditions that must be satisfied before the oracle comparator is used
	 */
	public OracleComparator(String id, Comparator oracle, List<Condition> preConditions) {
		this(id, oracle);
		this.preConditions.addAll(preConditions);
	}

	/**
	 * @param id
	 *            an identifier for the oracle
	 * @param oracle
	 *            the Oracle
	 * @param preConditions
	 *            the preconditions that must be satisfied before the oracle comparator is used
	 */
	public OracleComparator(String id, Comparator oracle, Condition... preConditions) {
		this(id, oracle);
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
	public Comparator getOracle() {
		return oracle;
	}

	/**
	 * @return the preconditions
	 */
	public List<Condition> getPreConditions() {
		return preConditions;
	}

}
