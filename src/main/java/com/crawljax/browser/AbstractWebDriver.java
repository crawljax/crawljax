package com.crawljax.browser;

import java.io.File;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.RenderedWebElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.crawljax.core.CrawljaxException;
import com.crawljax.core.state.Eventable;
import com.crawljax.util.Helper;
import com.crawljax.util.PropertyHelper;

/**
 * @author mesbah
 * @version $Id: AbstractWebDriver.java 6426 2009-12-29 17:21:03Z stefan $
 */
public class AbstractWebDriver implements EmbeddedBrowser {
	private static Logger logger = Logger.getLogger(WebDriver.class.getName());
	private WebDriver browser;

	/**
	 * @param browser
	 *            the browser to set
	 */
	protected void setBrowser(WebDriver browser) {
		this.browser = browser;
	}

	/**
	 * Public constructor.
	 * 
	 * @param logger
	 *            the log4j logger.
	 */
	public AbstractWebDriver(Logger logger) {
		AbstractWebDriver.logger = logger;
	}

	/**
	 * Constructor.
	 * 
	 * @param driver
	 *            The WebDriver to use.
	 */
	public AbstractWebDriver(WebDriver driver) {
		this.browser = driver;
	}

	/**
	 * @param url
	 *            The URL.
	 * @throws InterruptedException
	 * @throws CrawljaxException
	 *             if fails.
	 */
	public void goToUrl(String url) throws CrawljaxException {
		browser.navigate().to(url);
		try {
			Thread.sleep(PropertyHelper.getCrawlWaitReloadValue());
		} catch (InterruptedException e) {
			throw new CrawljaxException(e.getMessage(), e);
		}

	}

	/**
	 * Fires the event and waits for a specified time.
	 * 
	 * @param webElement
	 * @param eventable
	 *            The HTML event type (onclick, onmouseover, ...).
	 * @throws Exception
	 * @throws Exception
	 *             if fails.
	 */
	private boolean fireEventWait(WebElement webElement, Eventable eventable)
	        throws CrawljaxException {
		String eventType = eventable.getEventType();

		if ("onclick".equals(eventType)) {
			try {
				webElement.click();
			} catch (ElementNotVisibleException e1) {
				logger.info("Element not visible, so cannot be clicked: "
				        + webElement.getTagName().toUpperCase() + " " + webElement.getText());
				return false;
			} catch (Exception e) {
				logger.error(e.getMessage());
				return false;
			}
		} else {
			logger.info("EventType " + eventType + " not supported in WebDriver.");
		}

		try {
			Thread.sleep(PropertyHelper.getCrawlWaitEventValue());
		} catch (InterruptedException e) {
			throw new CrawljaxException(e.getMessage(), e);
		}
		return true;
	}

	/**
	 * @see EmbeddedBrowser#close()
	 */
	public void close() {
		logger.info("Closing the browser...");
		// close browser and close every associated window.
		browser.quit();
	}

	/**
	 * @return a string representation of the borser's DOM.
	 * @throws CrawljaxException
	 *             if fails.
	 * @see com.crawljax.browser.EmbeddedBrowser#getDom()
	 */
	public String getDom() throws CrawljaxException {
		try {
			return Helper.toUniformDOM(browser.getPageSource());
		} catch (Exception e) {
			throw new CrawljaxException(e.getMessage(), e);
		}
	}

	/**
	 * @see com.crawljax.browser.EmbeddedBrowser#goBack()
	 */
	public void goBack() {
		browser.navigate().back();
	}

	/**
	 * @return true if succeeds.
	 * @see com.crawljax.browser.EmbeddedBrowser#canGoBack()
	 */
	public boolean canGoBack() {
		// NOT IMPLEMENTED
		return false;
	}

	/**
	 * @param clickable
	 *            The clickable object.
	 * @param text
	 *            The input.
	 * @return true if succeeds.
	 */
	public boolean input(Eventable clickable, String text) {
		WebElement field = browser.findElement(clickable.getIdentification().getWebDriverBy());

		if (field != null) {
			field.sendKeys(text);

			// this.activeElement = field;
			return true;
		}

		return false;
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

			WebElement webElement =
			        browser.findElement(eventable.getIdentification().getWebDriverBy());

			if (webElement != null) {
				return fireEventWait(webElement, eventable);
			}
			return false;
		} catch (NoSuchElementException e) {
			logger.info("Could not fire eventable: " + eventable.toString());
			return false;
		} catch (RuntimeException e) {
			logger.error("Caught Exception: " + e.getMessage(), e);

			return false;
		}
	}

	/**
	 * @return the browser instance.
	 */
	public WebDriver getBrowser() {
		return browser;
	}

	/**
	 * Execute JavaScript in the browser.
	 * 
	 * @param code
	 *            The code to execute.
	 * @return The return value of the JavaScript.
	 */
	public Object executeJavaScript(String code) {
		JavascriptExecutor js = (JavascriptExecutor) browser;
		return js.executeScript(code);
	}

	/**
	 * Determines whether locater is visible.
	 * 
	 * @param locater
	 *            The element to search for.
	 * @return Whether it is visible.
	 */
	public boolean isVisible(By locater) {
		try {
			WebElement el = browser.findElement(locater);
			return ((RenderedWebElement) el).isDisplayed();
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * @return The current browser url.
	 */
	public String getCurrentUrl() {
		return browser.getCurrentUrl();
	}

	/**
	 * @return the webdriver instance.
	 */
	public WebDriver getDriver() {
		return this.browser;
	}

	@Override
	public void closeOtherWindows() {
		if (browser instanceof FirefoxDriver) {
			for (String handle : browser.getWindowHandles()) {
				if (!handle.equals(browser.getWindowHandle())) {
					String current = browser.getWindowHandle();
					browser.switchTo().window(handle);
					logger.info("Closing other window with title \"" + browser.getTitle() + "\"");
					browser.close();
					browser.switchTo().window(current);
				}
			}
		}

	}

	/**
	 * @param file
	 *            the file to write to the filename to save the screenshot in.
	 */
	public void saveScreenShot(File file) {
		if (browser instanceof FirefoxDriver) {
			((FirefoxDriver) browser).saveScreenshot(file);
			removeCanvasGeneratedByFirefoxDriverForScreenshots();
		} else {
			logger.warn("Screenshot not supported.");
		}
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
			logger.info("Could not remove the screenshot canvas from the DOM.");
		}
	}
}