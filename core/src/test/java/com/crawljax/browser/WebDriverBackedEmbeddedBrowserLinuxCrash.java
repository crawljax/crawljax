// Copyright 2010 Google Inc. All Rights Reserved.

package com.crawljax.browser;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.crawljax.core.CrawljaxException;
import com.crawljax.core.exception.BrowserConnectionException;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.forms.FormInput;

/**
 * This test is to check what the results are for the {@link EmbeddedBrowser} implemented by
 * {@link WebDriverBackedEmbeddedBrowser} when the used WebDriver is crashed. This 'UnitTest' runs
 * on Linux only and is not part of the default test-suite.
 */
public class WebDriverBackedEmbeddedBrowserLinuxCrash extends FirefoxLinuxCrash {

	private EmbeddedBrowser browser;

	/**
	 * Calls super.setUp to prepare the 'broken' ff-driver and load it into a
	 * WebDriverBackedEmbeddedBrowser.
	 * 
	 * @throws InterruptedException
	 *             when the sleep of supper is interrupt
	 */
	@Override
	@Before
	public void setUp() throws InterruptedException {
		super.setUp();
		browser = WebDriverBackedEmbeddedBrowser.withDriver(getCrashedDriver());
	}

	/**
	 * Test method for {@link com.crawljax.browser.EmbeddedBrowser#close()}.
	 */
	@Test(expected = BrowserConnectionException.class)
	public void testClose() {
		browser.close();
	}

	/**
	 * Test method for {@link com.crawljax.browser.EmbeddedBrowser#closeOtherWindows()}.
	 */
	@Test(expected = BrowserConnectionException.class)
	public void testCloseOtherWindows() {
		browser.closeOtherWindows();
	}

	/**
	 * Test method for
	 * {@link com.crawljax.browser.EmbeddedBrowser#executeJavaScript(java.lang.String)}.
	 * 
	 * @throws CrawljaxException
	 *             when the script can not be executed
	 */
	@Test(expected = BrowserConnectionException.class)
	public void testExecuteJavaScript() throws CrawljaxException {
		browser.executeJavaScript("return 'hi';");
	}

	/**
	 * Test method for
	 * {@link com.crawljax.browser.EmbeddedBrowser#fireEvent(com.crawljax.core.state.Eventable)}.
	 * 
	 * @throws CrawljaxException
	 *             when the event can not be fired.
	 */
	@Test(expected = BrowserConnectionException.class)
	public void testFireEvent() throws CrawljaxException {
		browser.fireEvent(new Eventable(new Identification(How.xpath, "/HTML"), EventType.click));
	}

	/**
	 * Test method for {@link com.crawljax.browser.EmbeddedBrowser#getCurrentUrl()}.
	 */
	@Test(expected = BrowserConnectionException.class)
	public void testGetCurrentUrl() {
		browser.getCurrentUrl();
	}

	/**
	 * Test method for {@link com.crawljax.browser.EmbeddedBrowser#getDom()}.
	 * 
	 * @throws CrawljaxException
	 *             when the dom can not be downloaded.
	 */
	@Test(expected = BrowserConnectionException.class)
	public void testGetDom() throws CrawljaxException {
		browser.getDom();
	}

	/**
	 * Test method for {@link com.crawljax.browser.EmbeddedBrowser#getDomWithoutIframeContent()}.
	 * 
	 * @throws CrawljaxException
	 *             when the the dom can not be downloaded.
	 */
	@Test(expected = BrowserConnectionException.class)
	public void testGetDomWithoutIframeContent() throws CrawljaxException {
		browser.getDomWithoutIframeContent();
	}

	/**
	 * Test method for {@link com.crawljax.browser.EmbeddedBrowser#goBack()}.
	 */
	@Test(expected = BrowserConnectionException.class)
	public void testGoBack() {
		browser.goBack();
	}

	/**
	 * Test method for {@link com.crawljax.browser.EmbeddedBrowser#goToUrl(java.lang.String)}.
	 * 
	 * @throws CrawljaxException
	 *             when the url can not be opend.
	 */
	@Test(expected = BrowserConnectionException.class)
	public void testGoToUrl() throws CrawljaxException {
		browser.goToUrl("http://www.google.com");
	}

	/**
	 * Test method for
	 * {@link com.crawljax.browser.EmbeddedBrowser#input(com.crawljax.core.state.Identification, java.lang.String)}
	 * .
	 * 
	 * @throws CrawljaxException
	 *             when the input can not be found
	 */
	@Test(expected = BrowserConnectionException.class)
	public void testInput() throws CrawljaxException {
		browser.input(new Identification(How.xpath, "/HTML"), "some");
	}

	/**
	 * Test method for
	 * {@link com.crawljax.browser.EmbeddedBrowser#isVisible(com.crawljax.core.state.Identification)}
	 * .
	 */
	@Test(expected = BrowserConnectionException.class)
	public void testIsVisible() {
		browser.isVisible(new Identification(How.xpath, "/HTML"));
	}

	/**
	 * Test method for
	 * {@link com.crawljax.browser.EmbeddedBrowser#getInputWithRandomValue(com.crawljax.forms.FormInput)}
	 * .
	 */
	@Test(expected = BrowserConnectionException.class)
	public void testGetInputWithRandomValue() {
		browser.getInputWithRandomValue(new FormInput("text", new Identification(How.xpath,
		        "/HTML"), "abc"));
	}

	/**
	 * Test method for {@link com.crawljax.browser.EmbeddedBrowser#getFrameDom(java.lang.String)}.
	 */
	@Test(expected = BrowserConnectionException.class)
	public void testGetFrameDom() {
		browser.getFrameDom("123");
	}

	/**
	 * Test method for
	 * {@link com.crawljax.browser.EmbeddedBrowser#elementExists(com.crawljax.core.state.Identification)}
	 * .
	 */
	@Test(expected = BrowserConnectionException.class)
	public void testElementExists() {
		browser.elementExists(new Identification(How.xpath, "/HTML"));
	}

	/**
	 * Test method for
	 * {@link com.crawljax.browser.EmbeddedBrowser#getWebElement(com.crawljax.core.state.Identification)}
	 * .
	 */
	@Test(expected = BrowserConnectionException.class)
	public void testGetWebElement() {
		browser.getWebElement(new Identification(How.xpath, "/HTML"));
	}

	/**
	 * Test method for {@link com.crawljax.browser.EmbeddedBrowser#saveScreenShot(java.io.File)}.
	 * 
	 * @throws CrawljaxException
	 *             when screenshotting failed
	 */
	@Test(expected = BrowserConnectionException.class)
	public void testSaveScreenShot() throws CrawljaxException {
		browser.saveScreenShot(new File("/tmp/file"));
	}
}
