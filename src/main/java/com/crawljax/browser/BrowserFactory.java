/**
 * Created Jan 18, 2008
 */
package com.crawljax.browser;

import com.crawljax.util.PropertyHelper;

/**
 * The factory class returns an instance of the desired browser as specified in the properties file.
 * 
 * @author mesbah
 * @version $Id$
 */
public final class BrowserFactory {

	/**
	 * hidden constructor.
	 */
	private BrowserFactory() {
	}

	/**
	 * Factory method returning an embedded browser implementation depending on the value of the
	 * "browser" in the properties file.
	 * 
	 * @return an embedded browser implementation.
	 * @throws Exception
	 */
	public static EmbeddedBrowser getBrowser() {
		if (PropertyHelper.getCrawljaxConfiguration() != null) {
			return PropertyHelper.getCrawljaxConfiguration().getBrowser();
		}
		String browser = PropertyHelper.getBrowserValue();

		if ("webdriver.ie".equals(browser)) {
			return new WebDriverIE();
		}

		return new WebDriverFirefox();
	}
}
