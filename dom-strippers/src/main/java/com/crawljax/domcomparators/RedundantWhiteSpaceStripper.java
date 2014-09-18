package com.crawljax.domcomparators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Change any whitespace, linebreak and tab character from the DOM to a single space character.
 *
 */
public class RedundantWhiteSpaceStripper implements DomStripper {

	private static final Pattern PATTERN = Pattern.compile("\\s+", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	@Override
	public String apply(String input) {
		StringBuffer result = new StringBuffer();
		Matcher matcher = PATTERN.matcher(input);
		while (matcher.find()) {
			matcher.appendReplacement(result, " ");
		}
		matcher.appendTail(result);
		return result.toString();
	}
}
