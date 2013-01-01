package com.crawljax.browser;

import java.io.File;

import org.openqa.selenium.WebElement;

import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawljaxConfigurationReader;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification;
import com.crawljax.forms.FormInput;

/**
 * Browser interface used by Crawjax.
 */
public interface EmbeddedBrowser {

	/**
	 * Browser types.
	 */
	public enum BrowserType {
		firefox, ie, chrome, remote, htmlunit, android, iphone
	}

	/**
	 * Opens the url in the browser.
	 * 
	 * @param url
	 *            the URL.
	 */
	void goToUrl(String url);

	/**
	 * fires the event.
	 * 
	 * @param event
	 *            the event.
	 * @return if fails.
	 */
	boolean fireEvent(Eventable event);

	/**
	 * @return the DOM string with all the iframe content.
	 */
	String getDom();

	/**
	 * @return the DOM string WITHOUT the iframe content.
	 */
	String getDomWithoutIframeContent();

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
	 * @param identification
	 *            the identification.
	 * @param text
	 *            the text.
	 * @return true if succeeded.
	 * @throws CrawljaxException
	 *             if fails.
	 */
	boolean input(Identification identification, String text) throws CrawljaxException;

	/**
	 * Execute JavaScript in the browser.
	 * 
	 * @param script
	 *            The script to execute.
	 * @return The JavaScript return object.
	 * @throws CrawljaxException
	 *             On error.
	 */
	Object executeJavaScript(String script) throws CrawljaxException;

	/**
	 * Checks if an element is visible.
	 * 
	 * @param identification
	 *            identification to use.
	 * @return true if the element is visible.
	 */
	boolean isVisible(Identification identification);

	/**
	 * @return The current browser url.
	 */
	String getCurrentUrl();

	/**
	 * @param inputForm
	 *            the input form.
	 * @return a FormInput filled with random data.
	 */
	FormInput getInputWithRandomValue(FormInput inputForm);

	/**
	 * @param iframeIdentification
	 *            the iframe's name or id.
	 * @return the DOM string of the corresponding iframe.
	 */
	String getFrameDom(String iframeIdentification);

	/**
	 * @param identification
	 *            the identification of the element to be checked.
	 * @return true if the element can be found in the browser's DOM tree.
	 */
	boolean elementExists(Identification identification);

	/**
	 * @param identification
	 *            the identification of the element to be found.
	 * @return the corresponding WebElement from the browser.
	 */
	WebElement getWebElement(Identification identification);

	/**
	 * @param file
	 *            the file to write the screenshot to (png).
	 * @throws CrawljaxException
	 *             if saving screenshots is not supported by the implementing class.
	 */
	void saveScreenShot(File file) throws CrawljaxException;

	/**
	 * Update the configuration of the Browser. When this method is called the implementing
	 * EmbeddedBrowser must updates its internal configuration to the values given as argument.
	 * 
	 * @param configuration
	 *            the new configuration values that needs to be updated.
	 */
	void updateConfiguration(CrawljaxConfigurationReader configuration);
}
