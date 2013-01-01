package com.crawljax.oraclecomparator.comparators;

import java.util.ArrayList;
import java.util.List;

import com.crawljax.oraclecomparator.AbstractComparator;
import com.crawljax.util.Helper;

/**
 * Regex oracles that strips content from the DOM to check whether the DOMs are equal without the
 * specified regular expressions.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
public class RegexComparator extends AbstractComparator {

	// NOTE: the ordering can be important
	private final List<String> regexs = new ArrayList<String>();

	/**
	 * Default Constructor without any regular expressions.
	 */
	public RegexComparator() {
	}

	/**
	 * @param regexs
	 *            the regular expressions
	 */
	public RegexComparator(String... regexs) {
		for (String regex : regexs) {
			this.regexs.add(regex);
		}
	}

	@Override
	public boolean isEquivalent() {
		for (String regex : regexs) {
			setOriginalDom(Helper.replaceString(getOriginalDom(), regex, ""));
			setNewDom(Helper.replaceString(getNewDom(), regex, ""));
		}
		return super.compare();
	}

	/**
	 * Add a number of regular expression.
	 * 
	 * @param regexs
	 *            The regular expressions.
	 */
	public void addRegularExpressions(List<String> regexs) {
		this.regexs.addAll(regexs);
	}

}
