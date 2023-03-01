package com.crawljax.oraclecomparator;

import com.crawljax.condition.Condition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.jcip.annotations.Immutable;

/**
 * This class contains the oracle and its precondition(s).
 */
@Immutable
public class OracleComparator {

    private final List<Condition> preConditions = new ArrayList<>();
    private final String id;
    private final Comparator oracle;

    /**
     * @param id     an identifier for the oracle
     * @param oracle the Oracle
     */
    public OracleComparator(String id, Comparator oracle) {
        this.id = id;
        this.oracle = oracle;
    }

    /**
     * @param id            an identifier for the oracle
     * @param oracle        the Oracle
     * @param preConditions the preconditions that must be satisfied before the oracle comparator is
     *                      used
     */
    public OracleComparator(String id, Comparator oracle, List<Condition> preConditions) {
        this(id, oracle);
        this.preConditions.addAll(preConditions);
    }

    /**
     * @param id            an identifier for the oracle
     * @param oracle        the Oracle
     * @param preConditions the preconditions that must be satisfied before the oracle comparator is
     *                      used
     */
    public OracleComparator(String id, Comparator oracle, Condition... preConditions) {
        this(id, oracle);
        this.preConditions.addAll(Arrays.asList(preConditions));
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
