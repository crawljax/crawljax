package com.crawljax.condition;

import net.jcip.annotations.Immutable;

import com.crawljax.browser.EmbeddedBrowser;

/**
 * A condition which returns true iff the expression does NOT occur in the DOM.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
@Immutable
public class NotRegexCondition extends AbstractCondition {

	private final RegexCondition regexCondition;

	/**
	 * @param expression
	 *            the regular expression.
	 */
	public NotRegexCondition(String expression) {
		this.regexCondition = new RegexCondition(expression);
	}

	@Override
	public boolean check(EmbeddedBrowser browser) {
		return Logic.not(regexCondition).check(browser);
	}

}
