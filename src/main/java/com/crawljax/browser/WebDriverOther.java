package com.crawljax.browser;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

/**
 * This class is a Deprecated placeholder class to support the new clone meganism. Its currenly only
 * used in the CrawljaxConfiguration when only a single Driver is specified.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id$
 */
@Deprecated
public class WebDriverOther extends AbstractWebDriver {

	@Deprecated
	private final WebDriver webDriver;

	/**
	 * Creates a new WebDriverOther object.
	 * 
	 * @param driver
	 *            the Generic WebDriver driver to use
	 */
	@Deprecated
	public WebDriverOther(WebDriver driver) {
		super(Logger.getLogger(WebDriverOther.class.getName()));
		setBrowser(driver);
		webDriver = driver;
	}

	@Override
	@Deprecated
	public EmbeddedBrowser clone() {
		/**
		 * This is totaly horrible!!
		 */
		try {
			return new WebDriverOther(webDriver.getClass().newInstance());
		} catch (InstantiationException e) {
			Logger.getLogger(WebDriverOther.class.getName()).error(
			        "InstantiationException catched this is horrible!", e);
		} catch (IllegalAccessException e) {
			Logger.getLogger(WebDriverOther.class.getName()).error(
			        "IllegalAccessException catched this is horrible!", e);
		}
		Logger.getLogger(WebDriverOther.class.getName()).error(
		        "Now returning a new Firefox Driver!");
		return new WebDriverOther(new FirefoxDriver());
	}
}
