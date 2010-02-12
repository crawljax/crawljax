package com.crawljax.browser;

import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import com.crawljax.core.CrawljaxException;
import com.crawljax.core.state.Eventable;

/**
 * @author amesbah
 * @version $Id: WebDriverChrome.java 218 amesbah $
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
	public EmbeddedBrowser clone() {
		return new WebDriverChrome(new ChromeDriver(), getFilterAttributes(),
		        getCrawlWaitReload(), getCrawlWaitEvent());
	}

	/**
	 * Fires an event on an element using its identification.
	 * 
	 * @param eventable
	 *            The eventable.
	 * @return true if it is able to fire the event successfully on the element.
	 * @throws CrawljaxException
	 *             On failure.
	 */
	public synchronized boolean fireEvent(Eventable eventable) throws CrawljaxException {
		try {

			boolean result = false;

			if (eventable.getRelatedFrame() != null && !eventable.getRelatedFrame().equals("")) {
				// driver.switchTo().frame(eventable.getRelatedFrame());
				LOGGER.warn("iframe not supported by ChromeDriver");
			}

			WebElement webElement =
			        driver.findElement(eventable.getIdentification().getWebDriverBy());

			if (webElement != null) {
				result = super.fireEventWait(webElement, eventable);
			}

			return result;

		} catch (NoSuchElementException e) {

			LOGGER.warn("Could not fire eventable: " + eventable.toString());
			return false;
		} catch (RuntimeException e) {
			LOGGER.error("Caught Exception: " + e.getMessage(), e);

			return false;
		}
	}

}
