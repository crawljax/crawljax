package com.crawljax.condition;

import com.crawljax.browser.EmbeddedBrowser;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import net.jcip.annotations.Immutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Condition that returns true iff expression occurs in the dom.
 */
@Immutable
public class RegexCondition implements Condition {

	private static final Logger LOG = LoggerFactory.getLogger(RegexCondition.class);
	private final String expression;
	private final Pattern pattern;

	/**
	 * @param expression the regular expression.
	 */
	public RegexCondition(String expression) {
		this.expression = expression;
		pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
	}

	@Override
	public boolean check(EmbeddedBrowser browser) {
		String dom = browser.getStrippedDom();
		boolean found = pattern.matcher(dom).find();
		if (found) {
			LOG.trace("Found expression {} in DOM {}", expression, dom);
		}
		return found;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getClass(), expression, pattern.toString());
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof RegexCondition) {
			RegexCondition that = (RegexCondition) object;
			return Objects.equal(this.expression, that.expression)
					&& Objects.equal(this.pattern.toString(), that.pattern.toString());
		}
		return false;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("expression", expression)
				.add("pattern", pattern.toString())
				.toString();
	}

}
