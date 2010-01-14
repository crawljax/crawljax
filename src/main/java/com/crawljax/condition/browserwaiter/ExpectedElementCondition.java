package com.crawljax.condition.browserwaiter;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.crawljax.browser.AbstractWebDriver;
import com.crawljax.browser.EmbeddedBrowser;

/**
 * Checks whether an elements exists.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
@ThreadSafe
public class ExpectedElementCondition implements ExpectedCondition {

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
	@GuardedBy("browser, driver")
	public boolean isSatisfied(EmbeddedBrowser browser) {
		if (browser instanceof AbstractWebDriver) {
			WebDriver driver = ((AbstractWebDriver) browser).getDriver();
			synchronized (browser) {
				synchronized (driver) {
					try {
						WebElement el = driver.findElement(locater);
						return el != null;
					} catch (Exception e) {
						return false;
					}
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.locater;
	}

}
