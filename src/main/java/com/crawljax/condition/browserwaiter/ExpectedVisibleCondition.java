package com.crawljax.condition.browserwaiter;

import org.openqa.selenium.By;

/**
 * Checks whether an element is visible.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
public class ExpectedVisibleCondition extends AbstractExpectedCondition {

	private final By locater;

	/**
	 * Constructor.
	 * @param locater Locater to use.
	 */
	public ExpectedVisibleCondition(By locater) {
		this.locater = locater;
	}

	@Override
	public boolean isSatisfied() {
		return getBrowser().isVisible(locater);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.locater;
	}

}
