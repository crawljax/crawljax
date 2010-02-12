package com.crawljax.browser;

import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.chrome.ChromeDriver;

/**
 * @author amesbah
 * @version $Id$
 */
public class WebDriverChrome extends AbstractWebDriver {

	private static final Logger LOGGER = Logger.getLogger(WebDriverChrome.class.getName());

	/**
	 * Creates a new WebDriverChrome object.
	 * 
	 * @param driver
	 *            the WebDriver ChromeDriver.
	 * @param filterAttributes
	 *            the attributes to be filtered from DOM.
	 * @param crawlWaitReload
	 *            the period to wait after a reload.
	 * @param crawlWaitEvent
	 *            the period to wait after an event is fired.
	 */
	public WebDriverChrome(ChromeDriver driver, List<String> filterAttributes,
	        long crawlWaitReload, long crawlWaitEvent) {
		super(driver, LOGGER, filterAttributes, crawlWaitReload, crawlWaitEvent);
	}

	/**
	 * the empty constructor.
	 * 
	 * @param filterAttributes
	 *            the attributes to be filtered from DOM.
	 * @param crawlWaitReload
	 *            the period to wait after a reload.
	 * @param crawlWaitEvent
	 *            the period to wait after an event is fired.
	 */
	public WebDriverChrome(List<String> filterAttributes, long crawlWaitReload,
	        long crawlWaitEvent) {
		this(new ChromeDriver(), filterAttributes, crawlWaitReload, crawlWaitEvent);
	}

	@Override
	public EmbeddedBrowser clone() {
		return new WebDriverChrome(new ChromeDriver(), getFilterAttributes(),
		        getCrawlWaitReload(), getCrawlWaitEvent());
	}

}
