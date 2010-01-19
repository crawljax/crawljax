package com.crawljax.browser;

import java.io.File;

import org.apache.log4j.Logger;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import com.crawljax.core.configuration.ProxyConfiguration;

/**
 * The class representing a Firefox webbrowser extending the AbstractWebDriver.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @author mesbah
 * @version $Id$
 */
public class WebDriverFirefox extends AbstractWebDriver {

	private ProxyConfiguration proxyConfiguration = null;
	private String firefoxLocation = null;

	/**
	 * Creates a new FirefoxDriver object based on a given driver as WebDriver.
	 */
	private WebDriverFirefox(FirefoxDriver driver) {
		super(Logger.getLogger(WebDriverFirefox.class.getName()));
		setBrowser(driver);
	}

	/**
	 * Creates a new FirefoxDriver object, use the default FirefoxDriver as WebDriver.
	 */
	public WebDriverFirefox() {
		this(new FirefoxDriver());
	}

	/**
	 * Creates a webdriver firefox instance with a proxy set.
	 * 
	 * @param config
	 *            Proxy configuration options.
	 */
	public WebDriverFirefox(ProxyConfiguration config) {
		this(new FirefoxDriver(makeProfile(config)));
		proxyConfiguration = config;
	}

	/**
	 * Creates a webdriver firefox instance for a given firefox location.
	 * 
	 * @param location
	 *            the location where to find the Firefox version to use
	 */
	public WebDriverFirefox(String location) {
		this(new FirefoxDriver(new FirefoxBinary(new File(location)), null));
		firefoxLocation = location;
	}

	private static final FirefoxProfile makeProfile(ProxyConfiguration config) {
		FirefoxProfile profile = new FirefoxProfile();
		profile.setPreference("network.proxy.http", config.getHostname());
		profile.setPreference("network.proxy.http_port", config.getPort());
		/* 1 means HTTP proxy */
		profile.setPreference("network.proxy.type", 1);
		return profile;
	}

	@Override
	public EmbeddedBrowser clone() {
		FirefoxBinary binary = null;
		FirefoxProfile profile = null;

		// If there is a proxyConfiguration; create a new Profile
		if (proxyConfiguration != null) {
			profile = makeProfile(proxyConfiguration);
		}

		// If the firefox location was specified, reuse the location
		if (firefoxLocation != null) {
			binary = new FirefoxBinary(new File(firefoxLocation));
		} else {
			binary = new FirefoxBinary();
		}

		return new WebDriverFirefox(new FirefoxDriver(binary, profile));
	}
}
