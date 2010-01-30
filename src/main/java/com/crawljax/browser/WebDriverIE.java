package com.crawljax.browser;

import org.apache.log4j.Logger;
import org.openqa.selenium.ie.InternetExplorerDriver;

/**
 * @author mesbah
 * @version $Id$
 */
public class WebDriverIE extends AbstractWebDriver {

	private static final Logger LOGGER = Logger.getLogger(WebDriverIE.class.getName());

	/**
	 * Creates a new WebDriverIE object.
	 * 
	 * @param driver
	 *            the WebDriver InternetExplorerDriver.
	 */
	public WebDriverIE(InternetExplorerDriver driver) {
		super(driver, LOGGER);
	}

	/**
	 * the empty constructor.
	 */
	public WebDriverIE() {
		this(new InternetExplorerDriver());
	}

	@Override
	public EmbeddedBrowser clone() {
		return new WebDriverIE();
	}

}
