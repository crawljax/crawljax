/**
 * Created Jan 8, 2008
 */
package com.crawljax.browser;

import org.openqa.selenium.WebElement;

import com.crawljax.core.CrawljaxException;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification;
import com.crawljax.forms.FormInput;

/**
 * @author mesbah
 * @version $Id$
 */
public interface EmbeddedBrowser extends Cloneable {

	/**
	 * Browser types.
	 */
	public enum BrowserType {
		firefox, ie, chrome
	}

	/**
	 * Opens the url in the browser.
	 * 
	 * @param url
	 *            the URL.
	 * @throws CrawljaxException
	 *             if fails.
	 */
	void goToUrl(String url) throws CrawljaxException;

	/**
	 * fires the event.
	 * 
	 * @param event
	 *            the event.
	 * @throws CrawljaxException
	 *             on Error.
	 * @return if fails.
	 */
	boolean fireEvent(Eventable event) throws CrawljaxException;

	/**
	 * @return the DOM string with all the iframe contents.
	 * @throws CrawljaxException
	 *             if fails.
	 */
	String getDom() throws CrawljaxException;

	/**
	 * @return the DOM string without the iframe contents
	 * @throws CrawljaxException
	 *             if fails.
	 */
	String getDomWithoutIframeContent() throws CrawljaxException;

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
	 * @param eventable
	 *            the event.
	 * @param text
	 *            the text.
	 * @return true if succeeded.
	 * @throws CrawljaxException
	 *             if fails.
	 */
	boolean input(Eventable eventable, String text) throws CrawljaxException;

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
	 * Make a clone of the current EmbeddedBrowser using the custom settings as provided earlier.
	 * 
	 * @return a new instance of a EmbeddedBrowser
	 */
	EmbeddedBrowser clone();

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
}
