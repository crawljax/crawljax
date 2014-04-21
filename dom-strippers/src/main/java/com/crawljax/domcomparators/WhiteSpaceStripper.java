package com.crawljax.domcomparators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Strips any whitespace, linebreak and tab character from the DOM.
 *
 * <p>This invalidates a DOM because {@code <DIV id='x'>} becomes {@code <DIVid='x'> } which is an invalid DOM
 * element</p>
 */
public class WhiteSpaceStripper implements DomStripper {

	private static final Pattern PATTERN = Pattern.compile("\\s+", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	@Override
	public String apply(String input) {
		StringBuffer result = new StringBuffer();
		Matcher matcher = PATTERN.matcher(input);
		while (matcher.find()) {
			matcher.appendReplacement(result, "");
		}
		matcher.appendTail(result);
		return result.toString();
	}
}
