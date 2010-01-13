/**
 * Created Jan 8, 2008
 */
package com.crawljax.browser;

import java.io.File;

import org.openqa.selenium.By;

import com.crawljax.core.CrawljaxException;
import com.crawljax.core.state.Eventable;

/**
 * @author mesbah
 * @version $Id$
 */
public interface EmbeddedBrowser {

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
	 * @return the DOM string.
	 * @throws CrawljaxException
	 *             if fails.
	 */
	String getDom() throws CrawljaxException;

	/**
	 * Closes the browser.
	 */
	void close();

	/**
	 * Closes all other windows.
	 */
	void closeOtherWindows();

	/**
	 * @return true if browser can go back to the previous state.
	 */
	boolean canGoBack();

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
	 * Creates a PNG screenshot of the browser in full screen mode.
	 * 
	 * @param pngFile
	 *            The filename to save the screenshot on.
	 */
	void saveScreenShot(File pngFile);

	/**
	 * Checks if an element is visible. TODO: replace By by Identification
	 * 
	 * @param locater
	 *            Locater to use.
	 * @return Whether it is visible.
	 */
	boolean isVisible(By locater);

	/**
	 * @return The current browser url.
	 */
	String getCurrentUrl();
}
