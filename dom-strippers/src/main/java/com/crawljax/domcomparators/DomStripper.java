package com.crawljax.domcomparators;

import com.google.common.base.Function;

/**
 * An interface that can be used to strip DOMs. The input and output of a {@link com.crawljax.domcomparators
 * .DomStripper} no not have to be valid DOMs.
 *
 * <p>Note that browser interaction is always done on the original DOM, not the modified dom return by a stripper</p>
 */
public interface DomStripper extends Function<String, String> {


	/**
	 * @param input The DOM that you can strip.
	 * @return the stripped DOM. Either the original object or a new object.
	 */
	@Override
	String apply(String input);
}
