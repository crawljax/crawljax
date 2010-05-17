package com.crawljax.browser;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.transaction.NotSupportedException;

import org.apache.log4j.Logger;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.crawljax.core.configuration.CrawljaxConfiguration;

/**
 * This class implements the remote web driver protocol, the client (this code) is run on the client
 * machine while the browser instance can be located on a other location specified by the
 * {@link CrawljaxConfiguration#setRemoteHubUrl(String)}.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id$
 */
public class WebDriverRemote extends AbstractWebDriver {
	private static final Logger LOGGER = Logger.getLogger(WebDriverRemote.class);

	/**
	 * The default WebDriver non aware constructor.
	 * 
	 * @param filterAttributes
	 *            the attributes to be filtered from DOM.
	 * @param crawlWaitReload
	 *            the period to wait after a reload.
	 * @param crawlWaitEvent
	 *            the period to wait after an event is fired.
	 * @param hubUrl
	 *            the location specified of the remote hub that needs to be taken to reach the
	 *            browser.
	 */
	public WebDriverRemote(List<String> filterAttributes, long crawlWaitReload,
	        long crawlWaitEvent, String hubUrl) {
		this(buildRemoteWebDriver(hubUrl), filterAttributes, crawlWaitReload, crawlWaitEvent);
	}

	/**
	 * The WebDriver aware constructor.
	 * 
	 * @param driver
	 *            the WebDriver that is used.
	 * @param filterAttributes
	 *            the attributes to be filtered from DOM.
	 * @param crawlWaitReload
	 *            the period to wait after a reload.
	 * @param crawlWaitEvent
	 *            the period to wait after an event is fired.
	 */
	public WebDriverRemote(WebDriver driver, List<String> filterAttributes, long crawlWaitReload,
	        long crawlWaitEvent) {
		super(driver, LOGGER, filterAttributes, crawlWaitReload, crawlWaitEvent);
	}

	@Override
	public EmbeddedBrowser clone() {
		// TODO Stefan: NOT SUPPORTED (YET)
		return null;
	}

	@Override
	public void saveScreenShot(File file) throws NotSupportedException {
		// TODO Must be done, Android & FF are capable of taking screenshots!
	}

	/**
	 * This method builds the RemoteWebDriver, it's not designed for extension; When the
	 * capabilities and or the browser specification needs to change a new EmbeddedBrowser class
	 * needs to be created that extends this class and has his own Constructor in which the new
	 * RemoteWebDriver is feed, the new Driver needs to be loaded using the {@link BrowserBuilder}.
	 * 
	 * @param hubUrl
	 *            the url to take to get to the remote browser.
	 * @return the new created RemoteWebDriver.
	 */
	private static RemoteWebDriver buildRemoteWebDriver(String hubUrl) {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setPlatform(Platform.ANY);
		URL url;
		try {
			url = new URL(hubUrl);
		} catch (MalformedURLException e) {
			LOGGER.error("The given hub url of the remote server is malformed can not continue!",
			        e);
			return null;
		}
		HttpCommandExecutor executor = null;
		try {
			executor = new HttpCommandExecutor(url);
		} catch (Exception e) {
			LOGGER.error("Received unknown exception while creating the "
			        + "HttpCommandExecutor, can not continue!", e);
			return null;
		}
		return new RemoteWebDriver(executor, capabilities);
	}
}
