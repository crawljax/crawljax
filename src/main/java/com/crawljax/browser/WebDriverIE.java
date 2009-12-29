package com.crawljax.browser;

import org.apache.log4j.Logger;
import org.openqa.selenium.ie.InternetExplorerDriver;

/**
 * @author mesbah
 * @version $Id: WebDriverIE.java 6297 2009-12-24 15:55:56Z mesbah $
 */
public class WebDriverIE extends AbstractWebDriver {

	/**
	 * Creates a new IEBrowser object.
	 */
	public WebDriverIE() {
		super(Logger.getLogger(WebDriverIE.class.getName()));
		setBrowser(new InternetExplorerDriver());
	}
}
