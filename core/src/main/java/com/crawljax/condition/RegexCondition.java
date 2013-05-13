package com.crawljax.condition;

import java.util.regex.Pattern;

import net.jcip.annotations.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;
import com.google.common.base.Objects;

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

	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), expression, pattern);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof RegexCondition) {
			if (!super.equals(object))
				return false;
			RegexCondition that = (RegexCondition) object;
			return Objects.equal(this.expression, that.expression)
			        && Objects.equal(this.pattern, that.pattern);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
		        .add("super", super.toString())
		        .add("expression", expression)
		        .add("pattern", pattern)
		        .toString();
	}

}
