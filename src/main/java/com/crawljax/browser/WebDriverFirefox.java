package com.crawljax.browser;

import java.io.File;
import java.util.List;

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
	private static final Logger LOGGER = Logger.getLogger(WebDriverFirefox.class.getName());
	private FirefoxDriver driver;

	/**
	 * Creates a new FirefoxDriver object based on a given driver as WebDriver.
	 */
	private WebDriverFirefox(FirefoxDriver driver, List<String> filterAttributes,
	        long crawlWaitReload, long crawlWaitEvent) {
		super(driver, LOGGER, filterAttributes, crawlWaitReload, crawlWaitEvent);
		this.driver = driver;
	}

	/**
	 * Creates a new FirefoxDriver object, use the default FirefoxDriver as WebDriver.
	 * 
	 * @param filterAttributes
	 *            the attributes to be filtered from DOM.
	 * @param crawlWaitReload
	 *            the period to wait after a reload.
	 * @param crawlWaitEvent
	 *            the period to wait after an event is fired.
	 */
	public WebDriverFirefox(List<String> filterAttributes, long crawlWaitReload,
	        long crawlWaitEvent) {
		this(new FirefoxDriver(), filterAttributes, crawlWaitReload, crawlWaitEvent);
	}

	/**
	 * Creates a webdriver firefox instance with a proxy set.
	 * 
	 * @param config
	 *            Proxy configuration options.
	 * @param filterAttributes
	 *            the attributes to be filtered from DOM.
	 * @param crawlWaitReload
	 *            the period to wait after a reload.
	 * @param crawlWaitEvent
	 *            the period to wait after an event is fired.
	 */
	public WebDriverFirefox(ProxyConfiguration config, List<String> filterAttributes,
	        long crawlWaitReload, long crawlWaitEvent) {
		this(new FirefoxDriver(makeProfile(config)), filterAttributes, crawlWaitReload,
		        crawlWaitEvent);
		proxyConfiguration = config;
	}

	/**
	 * Creates a webdriver firefox instance for a given firefox location.
	 * 
	 * @param location
	 *            the location where to find the Firefox version to use
	 * @param filterAttributes
	 *            the attributes to be filtered from DOM.
	 * @param crawlWaitReload
	 *            the period to wait after a reload.
	 * @param crawlWaitEvent
	 *            the period to wait after an event is fired.
	 */
	public WebDriverFirefox(String location, List<String> filterAttributes, long crawlWaitReload,
	        long crawlWaitEvent) {
		this(new FirefoxDriver(new FirefoxBinary(new File(location)), null), filterAttributes,
		        crawlWaitReload, crawlWaitEvent);
		firefoxLocation = location;
	}

	private static FirefoxProfile makeProfile(ProxyConfiguration config) {
		FirefoxProfile profile = new FirefoxProfile();
		profile.setPreference("network.proxy.http", config.getHostname());
		profile.setPreference("network.proxy.http_port", config.getPort());
		/* 1 means HTTP proxy */
		profile.setPreference("network.proxy.type", 1);
		/* use proxy for everything, including localhost */
		profile.setPreference("network.proxy.no_proxies_on", "");
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

		return new WebDriverFirefox(new FirefoxDriver(binary, profile), getFilterAttributes(),
		        getCrawlWaitReload(), getCrawlWaitEvent());
	}

	/**
	 * @param file
	 *            the file to write to the filename to save the screenshot in.
	 */
	public void saveScreenShot(File file) {

		driver.saveScreenshot(file);
		removeCanvasGeneratedByFirefoxDriverForScreenshots();

	}

	private void removeCanvasGeneratedByFirefoxDriverForScreenshots() {
		String js = "";
		js += "var canvas = document.getElementById('fxdriver-screenshot-canvas');";
		js += "if(canvas != null){";
		js += "canvas.parentNode.removeChild(canvas);";
		js += "}";
		try {
			executeJavaScript(js);
		} catch (Exception e) {
			LOGGER.warn("Could not remove the screenshot canvas from the DOM.");
		}
	}
}
