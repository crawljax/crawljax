package com.crawljax.domcomparators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Strips the head section
 *
 */
public class HeadStripper implements DomStripper {

	private static final Pattern PATTERN = Pattern.compile("<head>.*</head>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

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
