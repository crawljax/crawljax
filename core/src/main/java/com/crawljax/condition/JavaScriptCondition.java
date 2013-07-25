package com.crawljax.condition;

import net.jcip.annotations.Immutable;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawljaxException;
import com.google.common.base.Objects;

/**
 * A condition in the form of a JavaScript expression which returns true if the expression return
 * true.
 */
@Immutable
public class JavaScriptCondition implements Condition {

	private final String expression;

	/**
	 * Construct a JavaScript condition check.
	 * 
	 * @param expression
	 *            The actual Javascript to check.
	 */
	public JavaScriptCondition(String expression) {
		this.expression = expression;
	}

	/**
	 * Check invariant.
	 * 
	 * @param browser
	 *            The browser.
	 * @return Whether the condition is satisfied or <code>false</code> when it it isn't or a
	 *         {@link CrawljaxException} occurs.
	 */
	@Override
	public boolean check(EmbeddedBrowser browser) {
		String js =
		        "try{ if(" + expression + "){return '1';}else{" + "return '0';}}catch(e){"
		                + " return '0';}";
		try {
			Object object = browser.executeJavaScript(js);
			if (object == null) {
				return false;
			}
			return object.toString().equals("1");
		} catch (CrawljaxException e) {
			// Exception is caught, check failed so return false;
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getClass(), expression);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof JavaScriptCondition) {
			JavaScriptCondition that = (JavaScriptCondition) object;
			return Objects.equal(this.expression, that.expression);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
		        .add("expression", expression)
		        .toString();
	}

}
