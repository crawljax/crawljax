package com.crawljax.condition.browserwaiter;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.crawljax.browser.AbstractWebDriver;

/**
 * Checks whether an elements exists.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
public class ExpectedElementCondition extends AbstractExpectedCondition {

	private final By locater;

	/**
	 * Constructor.
	 * 
	 * @param locater
	 *            Locater to use.
	 */
	public ExpectedElementCondition(By locater) {
		this.locater = locater;
	}

	@Override
	public boolean isSatisfied() {
		if (getBrowser() instanceof AbstractWebDriver) {
			WebDriver driver = ((AbstractWebDriver) getBrowser()).getDriver();
			try {
				WebElement el = driver.findElement(locater);
				return el != null;
			} catch (Exception e) {
				return false;
			}
		}

		return false;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.locater;
	}

}
