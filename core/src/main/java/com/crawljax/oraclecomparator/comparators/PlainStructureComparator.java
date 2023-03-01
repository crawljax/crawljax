package com.crawljax.oraclecomparator.comparators;

import com.crawljax.oraclecomparator.AbstractComparator;

public class PlainStructureComparator extends AbstractComparator {

    private final boolean removeAttributes;

    public PlainStructureComparator() {
        this(true);
    }

    public PlainStructureComparator(boolean removeAttributes) {
        this.removeAttributes = removeAttributes;
    }

    @Override
    public String normalize(String dom) {
        String normalized = dom;
        if (removeAttributes) {
            normalized = stripAttributes(normalized);
        }
        return stripContent(normalized);
    }

    private String stripAttributes(String string) {
        String regExAttributes = "<(.+?)(\\s.*?)?(/)?>";
        return string.replaceAll(regExAttributes, "<$1$3>");
    }

    private String stripContent(String string) {
        String strippedStr;

        // remove line breaks
        strippedStr = string.replaceAll("[\\t\\n\\x0B\\f\\r]", "");

        // remove content
        strippedStr = strippedStr.replaceAll(">(.*?)<", "><");
        return strippedStr;
    }
}
