package com.crawljax.domcomparators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttributesStripper implements DomStripper {

	private static final Pattern GROUP_ELEMENTS = Pattern.compile("(<\\w+\\s)([^>]*)>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	@Override
	public String apply(String input) {
		StringBuffer out = new StringBuffer();
		Matcher matcher = GROUP_ELEMENTS.matcher(input);
		while (matcher.find()) {
			matcher.appendReplacement(out, matcher.group(1) + '>');
		}
		matcher.appendTail(out);
		return out.toString();
	}
}
