package com.crawljax.browser;

import java.io.File;
import java.util.List;

import javax.transaction.NotSupportedException;

import org.apache.log4j.Logger;
import org.openqa.selenium.ie.InternetExplorerDriver;

/**
 * The Internet Explorer implementation of AbstractWebdriver.
 * 
 * @author mesbah
 * @version $Id$
 */
public class WebDriverIE extends WebDriverBackedEmbeddedBrowser {

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
	public void saveScreenShot(File file) throws NotSupportedException {
		LOGGER.warn("screenshot not supprted by WebDriver IE");
		throw new NotSupportedException("screenshot not supprted by WebDriver IE");
	}

}
