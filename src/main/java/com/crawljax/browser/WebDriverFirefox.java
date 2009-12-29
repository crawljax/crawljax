package com.crawljax.browser;

import org.apache.log4j.Logger;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import com.crawljax.core.configuration.ProxyConfiguration;

/**
 * @author mesbah
 * @version $Id: WebDriverFirefox.java 6362 2009-12-28 15:18:08Z frank $
 */
public class WebDriverFirefox extends AbstractWebDriver {

	/**
	 * Creates a new FirefoxDriver object.
	 */
	public WebDriverFirefox() {
		super(Logger.getLogger(WebDriverFirefox.class.getName()));
		setBrowser(new FirefoxDriver());
	}

	/**
	 * Creates a webdriver firefox instance with a proxy set.
	 * 
	 * @param config
	 *            Proxy configuration options.
	 */
	public WebDriverFirefox(ProxyConfiguration config) {
		super(Logger.getLogger(WebDriverFirefox.class.getName()));

		FirefoxProfile profile = new FirefoxProfile();
		profile.setPreference("network.proxy.http", config.getHostname());
		profile.setPreference("network.proxy.http_port", config.getPort());
		/* 1 means HTTP proxy */
		profile.setPreference("network.proxy.type", 1);
		setBrowser(new FirefoxDriver(profile));
	}
}
