package com.crawljax.condition;

import org.openqa.selenium.By;

import com.crawljax.browser.EmbeddedBrowser;

/**
 * Conditions that returns true iff element found with By is visible.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id: NotVisibleCondition.java 6301 2009-12-24 16:36:24Z mesbah $
 */
public class NotVisibleCondition extends AbstractCondition {

	private final VisibleCondition visibleCondition;

	/**
	 * @param locater
	 *            the locator.
	 */
	public NotVisibleCondition(By locater) {
		this.visibleCondition = new VisibleCondition(locater);
	}

	@Override
	public boolean check(EmbeddedBrowser browser) {
		return Logic.not(visibleCondition).check(browser);
	}

}
