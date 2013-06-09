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
public class VisibleCondition extends AbstractCondition {

	private final Identification identification;

	/**
	 * @param identification
	 *            the identification.
	 */
	public VisibleCondition(Identification identification) {
		this.identification = identification;
	}

	@Override
	public boolean check(EmbeddedBrowser browser) {
		return browser.isVisible(identification);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
		        .add("identification", identification)
		        .toString();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), identification);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof VisibleCondition) {
			if (!super.equals(object)) {
				return false;
			}
			VisibleCondition that = (VisibleCondition) object;
			return Objects.equal(this.identification, that.identification);
		}
		return false;
	}

}
