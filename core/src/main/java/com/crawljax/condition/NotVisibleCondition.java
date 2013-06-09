package com.crawljax.condition;

import net.jcip.annotations.Immutable;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.state.Identification;
import com.google.common.base.Objects;

/**
 * Conditions that returns true iff element found with By is visible.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 */
@Immutable
public class NotVisibleCondition extends AbstractCondition {

	private final VisibleCondition visibleCondition;

	/**
	 * @param identification
	 *            the identification.
	 */
	public NotVisibleCondition(Identification identification) {
		this.visibleCondition = new VisibleCondition(identification);
	}

	@Override
	public boolean check(EmbeddedBrowser browser) {
		return Logic.not(visibleCondition).check(browser);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), visibleCondition);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof NotVisibleCondition) {
			if (!super.equals(object)) {
				return false;
			}
			NotVisibleCondition that = (NotVisibleCondition) object;
			return Objects.equal(this.visibleCondition, that.visibleCondition);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
		        .add("super", super.toString())
		        .add("visibleCondition", visibleCondition)
		        .toString();
	}

}
