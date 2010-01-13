package com.crawljax.browser;

import org.apache.log4j.Logger;
import org.openqa.selenium.ie.InternetExplorerDriver;

/**
 * @author mesbah
 * @version $Id$
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
