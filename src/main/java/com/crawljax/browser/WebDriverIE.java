package com.crawljax.browser;

import java.util.List;

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
	 * @param filterAttributes
	 *            the attributes to be filtered from DOM.
	 * @param crawlWaitReload
	 *            the period to wait after a reload.
	 * @param crawlWaitEvent
	 *            the period to wait after an event is fired.
	 */
	public WebDriverIE(InternetExplorerDriver driver, List<String> filterAttributes,
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
	public WebDriverIE(List<String> filterAttributes, long crawlWaitReload, long crawlWaitEvent) {
		this(new InternetExplorerDriver(), filterAttributes, crawlWaitReload, crawlWaitEvent);
	}

	@Override
	public EmbeddedBrowser clone() {
		return new WebDriverIE(new InternetExplorerDriver(), getFilterAttributes(),
		        getCrawlWaitReload(), getCrawlWaitEvent());
	}

}
