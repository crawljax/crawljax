package com.crawljax.condition;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.state.Identification;

/**
 * Conditions that returns true iff element found with By is visible.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
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

}
