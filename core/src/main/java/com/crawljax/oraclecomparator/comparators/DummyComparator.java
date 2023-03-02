package com.crawljax.oraclecomparator.comparators;

import com.crawljax.oraclecomparator.AbstractComparator;

/**
 * Simple oracle which can ignore whitespaces and line breaks.
 */
public class DummyComparator extends AbstractComparator {

    /**
     * Default argument less constructor.
     */
    public DummyComparator() {
        super();
    }

    @Override
    public String normalize(String string) {
        return string;
    }
}
