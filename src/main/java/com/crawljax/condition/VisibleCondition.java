package com.crawljax.condition;

import org.openqa.selenium.By;

import com.crawljax.browser.EmbeddedBrowser;

/**
 * Conditions that returns true iff element found with By is visible.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
public class VisibleCondition extends AbstractCondition {

	private final By locater;

	/**
	 * @param locater
	 *            the locator.
	 */
	public VisibleCondition(By locater) {
		this.locater = locater;
	}

	@Override
	public boolean check(EmbeddedBrowser browser) {
		return browser.isVisible(locater);
	}

}
