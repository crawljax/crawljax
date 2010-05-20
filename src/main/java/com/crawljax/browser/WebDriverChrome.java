package com.crawljax.browser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.chrome.ChromeDriver;

/**
 * Google Chrome implementation of AbstractWebDriver.
 * 
 * @author amesbah
 * @version $Id$
 */
public class WebDriverChrome extends AbstractWebDriver {

	private static final Logger LOGGER = Logger.getLogger(WebDriverChrome.class.getName());
	private ChromeDriver driver;

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
		this.driver = driver;
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
	public void closeOtherWindows() {
		LOGGER.info("Closing other windows not implemented for ChromeDriver");
	}

	/**
	 * @param file
	 *            the file to write to.
	 */
	public void saveScreenShot(File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			out.write(driver.getScreenshotAs(OutputType.BYTES));
			out.close();
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

	}
}
