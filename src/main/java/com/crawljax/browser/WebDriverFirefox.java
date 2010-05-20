package com.crawljax.browser;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.internal.FileHandler;

import com.crawljax.core.configuration.ProxyConfiguration;

/**
 * The class representing a Firefox webbrowser extending the AbstractWebDriver.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @author mesbah
 * @version $Id$
 */
public class WebDriverFirefox extends AbstractWebDriver {

	private static final Logger LOGGER = Logger.getLogger(WebDriverFirefox.class.getName());
	private final FirefoxDriver driver;

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
		this(makeProfile(config), filterAttributes, crawlWaitReload, crawlWaitEvent);
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
	}

	/**
	 * Creates a webdriver firefox instance for a given profile configuration.
	 * 
	 * @param profile
	 *            the profile configuration to read the config from
	 * @param filterAttributes
	 *            the attributes to be filtered from DOM.
	 * @param crawlWaitReload
	 *            the period to wait after a reload.
	 * @param crawlWaitEvent
	 *            the period to wait after an event is fired.
	 */
	public WebDriverFirefox(FirefoxProfile profile, List<String> filterAttributes,
	        long crawlWaitReload, long crawlWaitEvent) {
		this(new FirefoxDriver(profile), filterAttributes, crawlWaitReload, crawlWaitReload);
	}

	private static FirefoxProfile makeProfile(ProxyConfiguration config) {
		FirefoxProfile profile = new FirefoxProfile();
		profile.setPreference("network.proxy.http", config.getHostname());
		profile.setPreference("network.proxy.http_port", config.getPort());
		profile.setPreference("network.proxy.type", config.getType().toInt());
		/* use proxy for everything, including localhost */
		profile.setPreference("network.proxy.no_proxies_on", "");
		return profile;
	}

	/**
	 * @param file
	 *            the file to write to the filename to save the screenshot in.
	 */
	public void saveScreenShot(File file) {
		File tmpfile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

		try {
			FileHandler.copy(tmpfile, file);
		} catch (IOException e) {
			throw new WebDriverException(e);
		}

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
