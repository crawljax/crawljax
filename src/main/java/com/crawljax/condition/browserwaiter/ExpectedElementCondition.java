package com.crawljax.condition.browserwaiter;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.state.Identification;

/**
 * Checks whether an elements exists.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
@ThreadSafe
public class ExpectedElementCondition implements ExpectedCondition {

	private final Identification identification;

	/**
	 * Constructor.
	 * 
	 * @param identification
	 *            the identification to use.
	 */
	public ExpectedElementCondition(Identification identification) {
		this.identification = identification;
	}

	@Override
	@GuardedBy("browser")
	public boolean isSatisfied(EmbeddedBrowser<?> browser) {
		synchronized (browser) {
			try {
				return browser.elementExists(identification);
			} catch (Exception e) {
				return false;
			}
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.identification;
	}

}
