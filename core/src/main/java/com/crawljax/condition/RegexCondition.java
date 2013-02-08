package com.crawljax.condition;

import java.util.regex.Pattern;

import net.jcip.annotations.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;

/**
 * Condition that returns true iff experssion occurs in the dom.
 */
@Immutable
public class RegexCondition extends AbstractCondition {

	private static final Logger LOG = LoggerFactory.getLogger(RegexCondition.class);
	private final String expression;
	private final Pattern pattern;

	/**
	 * @param expression
	 *            the regular expression.
	 */
	public RegexCondition(String expression) {
		this.expression = expression;
		pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
	}

	@Override
	public boolean check(EmbeddedBrowser browser) {
		String dom = browser.getDom();
		boolean found = pattern.matcher(dom).find();
		if (found) {
			LOG.trace("Found expression {} in DOM {}", expression, dom);
		}
		return found;
	}
}
