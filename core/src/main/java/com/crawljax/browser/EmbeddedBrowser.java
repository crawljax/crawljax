package com.crawljax.browser;

import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.PreCrawlConfiguration;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification;
import com.crawljax.forms.FormInput;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;

/**
 * Browser interface used by Crawljax.
 */
public interface EmbeddedBrowser {

	/**
	 * Browser types.
	 */
	enum BrowserType {
		CHROME, CHROME_HEADLESS, FIREFOX, FIREFOX_HEADLESS, PHANTOMJS, REMOTE
	}

	/**
	 * Opens the url in the browser.
	 *
	 * @param url the URL.
	 */
	void goToUrl(URI url);

	/**
	 * fires the event.
	 *
	 * @param event the event.
	 * @return if fails.
	 */
	boolean fireEventAndWait(Eventable event)
			throws ElementNotVisibleException, InterruptedException;

	/**
	 * Removes the stripped items from {@link PreCrawlConfiguration#getFilterAttributeNames()}.
	 *
	 * @return the DOM string with all the iframe content.
	 */
	String getStrippedDom();

	/**
	 * Removes the stripped items from {@link PreCrawlConfiguration#getFilterAttributeNames()}.
	 *
	 * @return The dom without any elements stripped.
	 * @see WebDriver#getPageSource()
	 */
	String getUnStrippedDom();

	/**
	 * @return implemented by {@link #getStrippedDom()}.
	 * @deprecated use {@link #getStrippedDom()}.
	 */
	@Deprecated
	String getDom();

	/**
	 * @return the DOM string WITHOUT the iframe content.
	 */
	String getStrippedDomWithoutIframeContent();

	/**
	 * Closes the browser.
	 */
	void close();

	/**
	 * Closes all other windows.
	 */
	void closeOtherWindows();

	/**
	 * Go back to the previous state.
	 */
	void goBack();

	/**
	 * @param identification the identification.
	 * @param text           the text.
	 * @return true if succeeded.
	 * @throws CrawljaxException if fails.
	 */
	boolean input(Identification identification, String text) throws CrawljaxException;

	/**
	 * Execute JavaScript in the browser.
	 *
	 * @param script The script to execute.
	 * @return The JavaScript return object.
	 * @throws CrawljaxException On error.
	 */
	Object executeJavaScript(String script) throws CrawljaxException;

	/**
	 * Checks if an element is visible.
	 *
	 * @param identification identification to use.
	 * @return true if the element is visible.
	 */
	boolean isVisible(Identification identification);

	/**
	 * @return The current browser url.
	 */
	String getCurrentUrl();

	/**
	 * @param inputForm the input form.
	 * @return a FormInput filled with random data.
	 */
	FormInput getInputWithRandomValue(FormInput inputForm);

	/**
	 * @param iframeIdentification the iframe's name or id.
	 * @return the DOM string of the corresponding iframe.
	 */
	String getFrameDom(String iframeIdentification);

	/**
	 * @param identification the identification of the element to be checked.
	 * @return true if the element can be found in the browser's DOM tree.
	 */
	boolean elementExists(Identification identification);

	/**
	 * @param identification the identification of the element to be found.
	 * @return the corresponding WebElement from the browser.
	 */
	WebElement getWebElement(Identification identification);

	/**
	 * @return The selenium web driver.
	 * @author qhanam
	 */
	WebDriver getWebDriver();

	/**
	 * @param file the file to write the screenshot to (png).
	 * @throws CrawljaxException if saving screenshots is not supported by the implementing class.
	 */
	void saveScreenShot(File file) throws CrawljaxException;

	BufferedImage getScreenShotAsBufferedImage(int scrollTimeout) throws CrawljaxException;

	String getScreenShotAsBase64() throws CrawljaxException;

	void handlePopups();
}
