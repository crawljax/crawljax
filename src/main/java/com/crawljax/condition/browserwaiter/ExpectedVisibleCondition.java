package com.crawljax.condition.browserwaiter;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.openqa.selenium.By;

import com.crawljax.browser.EmbeddedBrowser;

/**
 * Checks whether an element is visible.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
@ThreadSafe
public class ExpectedVisibleCondition implements ExpectedCondition {

	private final By locater;

	/**
	 * Constructor.
	 * 
	 * @param locater
	 *            Locater to use.
	 */
	public ExpectedVisibleCondition(By locater) {
		this.locater = locater;
	}

	@Override
	@GuardedBy("browser")
	public boolean isSatisfied(EmbeddedBrowser browser) {
		synchronized (browser) {
			return browser.isVisible(locater);
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.locater;
	}

}
