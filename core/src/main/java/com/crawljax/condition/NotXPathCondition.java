package com.crawljax.condition;

import net.jcip.annotations.Immutable;

import com.crawljax.browser.EmbeddedBrowser;
import com.google.common.base.Objects;

/**
 * Condition that returns true iff no elements are found with expression.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 */
@Immutable
public class NotXPathCondition extends AbstractCondition {

	private final XPathCondition xpathCondition;

	/**
	 * @param expression
	 *            the XPath expression.
	 */
	public NotXPathCondition(String expression) {
		this.xpathCondition = new XPathCondition(expression);
	}

	@Override
	public boolean check(EmbeddedBrowser browser) {
		return Logic.not(xpathCondition).check(browser);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), xpathCondition);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof NotXPathCondition) {
			if (!super.equals(object)) {
				return false;
			}
			NotXPathCondition that = (NotXPathCondition) object;
			return Objects.equal(this.xpathCondition, that.xpathCondition);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
		        .add("super", super.toString())
		        .add("xpathCondition", xpathCondition)
		        .toString();
	}

}
