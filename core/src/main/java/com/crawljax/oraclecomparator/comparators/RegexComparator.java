package com.crawljax.oraclecomparator.comparators;

import java.util.Collection;

import com.crawljax.oraclecomparator.AbstractComparator;
import com.crawljax.util.DomUtils;
import com.google.common.collect.ImmutableList;

/**
 * Regex oracles that strips content from the DOM to check whether the DOMs are equal without the
 * specified regular expressions.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 */
public class RegexComparator extends AbstractComparator {

	// NOTE: the ordering can be important
	private final ImmutableList<String> regexs;

	public RegexComparator(Collection<String> regexs) {
		this.regexs = ImmutableList.copyOf(regexs);
	}

	public RegexComparator(String... regexs) {
		this.regexs = ImmutableList.copyOf(regexs);
	}

	@Override
	public String normalize(String dom) {
		String normalized = dom;
		for (String regex : regexs) {
			normalized = DomUtils.replaceString(normalized, regex, "");
		}
		return normalized;
	}

}
