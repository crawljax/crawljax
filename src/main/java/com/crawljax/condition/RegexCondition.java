package com.crawljax.condition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jcip.annotations.Immutable;

import com.crawljax.browser.EmbeddedBrowser;

/**
 * Condition that returns true iff experssion occurs in the dom.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
@Immutable
public class RegexCondition extends AbstractCondition {

	private final String expression;

	/**
	 * @param expression
	 *            the regular expression.
	 */
	public RegexCondition(String expression) {
		this.expression = expression;
	}

	@Override
	public boolean check(EmbeddedBrowser browser) {
		String dom = browser.getDom();
		Pattern p = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(dom);
		return m.find();
	}

}
