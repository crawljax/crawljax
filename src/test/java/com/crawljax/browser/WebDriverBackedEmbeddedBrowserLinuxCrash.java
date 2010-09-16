// Copyright 2010 Google Inc. All Rights Reserved.

package com.crawljax.browser;

import com.crawljax.core.CrawljaxException;
import com.crawljax.core.exception.BrowserConnectionException;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.forms.FormInput;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * This test is to check what the results are for the {@link EmbeddedBrowser} implemented by
 * {@link WebDriverBackedEmbeddedBrowser} when the used WebDriver is crashed. This 'UnitTest' runs
 * on Linux only and is not part of the default test-suite.
 *
 * @version $Id$
 * @author slenselink@google.com (Stefan Lenselink)
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
	@Test
	public final void testClose() {
		try {
			browser.close();
		} catch (BrowserConnectionException e) {
			// this is how its designed.
			return;
		}
		Assert.fail("A BrowserconnectionException must be thrown");
	}

	/**
	 * Test method for {@link com.crawljax.browser.EmbeddedBrowser#closeOtherWindows()}.
	 */
	@Test
	public final void testCloseOtherWindows() {
		try {
			browser.closeOtherWindows();
		} catch (BrowserConnectionException e) {
			// this is how its designed.
			return;
		}
		Assert.fail("A BrowserconnectionException must be thrown");
	}

	/**
	 * Test method for
	 * {@link com.crawljax.browser.EmbeddedBrowser#executeJavaScript(java.lang.String)}.
	 *
	 * @throws CrawljaxException
	 *             when the script can not be executed
	 */
	@Test
	public final void testExecuteJavaScript() throws CrawljaxException {
		try {
			browser.executeJavaScript("return 'hi';");
		} catch (BrowserConnectionException e) {
			// this is how its designed.
			return;
		}
		Assert.fail("A BrowserconnectionException must be thrown");
	}

	/**
	 * Test method for
	 * {@link com.crawljax.browser.EmbeddedBrowser#fireEvent(com.crawljax.core.state.Eventable)}.
	 *
	 * @throws CrawljaxException
	 *             when the event can not be fired.
	 */
	@Test
	public final void testFireEvent() throws CrawljaxException {
		try {
			browser.fireEvent(
			        new Eventable(new Identification(How.xpath, "/HTML"), EventType.click));
		} catch (BrowserConnectionException e) {
			// this is how its designed.
			return;
		}
		Assert.fail("A BrowserconnectionException must be thrown");
	}

	/**
	 * Test method for {@link com.crawljax.browser.EmbeddedBrowser#getCurrentUrl()}.
	 */
	@Test
	public final void testGetCurrentUrl() {
		try {
			browser.getCurrentUrl();
		} catch (BrowserConnectionException e) {
			// this is how its designed.
			return;
		}
		Assert.fail("A BrowserconnectionException must be thrown");
	}

	/**
	 * Test method for {@link com.crawljax.browser.EmbeddedBrowser#getDom()}.
	 *
	 * @throws CrawljaxException
	 *             when the dom can not be downloaded.
	 */
	@Test
	public final void testGetDom() throws CrawljaxException {
		try {
			browser.getDom();
		} catch (BrowserConnectionException e) {
			// this is how its designed.
			return;
		}
		Assert.fail("A BrowserconnectionException must be thrown");
	}

	/**
	 * Test method for {@link com.crawljax.browser.EmbeddedBrowser#getDomWithoutIframeContent()}.
	 *
	 * @throws CrawljaxException
	 *             when the the dom can not be downloaded.
	 */
	@Test
	public final void testGetDomWithoutIframeContent() throws CrawljaxException {
		try {
			browser.getDomWithoutIframeContent();
		} catch (BrowserConnectionException e) {
			// this is how its designed.
			return;
		}
		Assert.fail("A BrowserconnectionException must be thrown");
	}

	/**
	 * Test method for {@link com.crawljax.browser.EmbeddedBrowser#goBack()}.
	 */
	@Test
	public final void testGoBack() {
		try {
			browser.goBack();
		} catch (BrowserConnectionException e) {
			// this is how its designed.
			return;
		}
		Assert.fail("A BrowserconnectionException must be thrown");
	}

	/**
	 * Test method for {@link com.crawljax.browser.EmbeddedBrowser#goToUrl(java.lang.String)}.
	 *
	 * @throws CrawljaxException
	 *             when the url can not be opend.
	 */
	@Test
	public final void testGoToUrl() throws CrawljaxException {
		try {
			browser.goToUrl("http://www.google.com");
		} catch (BrowserConnectionException e) {
			// this is how its designed.
			return;
		}
		Assert.fail("A BrowserconnectionException must be thrown");
	}

	/**
	 * Test method for {@link
	 * com.crawljax.browser.EmbeddedBrowser#input(com.crawljax.core.state.Identification,
	 * java.lang.String)}.
	 *
	 * @throws CrawljaxException
	 *             when the input can not be found
	 */
	@Test
	public final void testInput() throws CrawljaxException {
		try {
			browser.input(new Identification(How.xpath, "/HTML"), "some");
		} catch (BrowserConnectionException e) {
			// this is how its designed.
			return;
		}
		Assert.fail("A BrowserconnectionException must be thrown");
	}

	/**
	 * Test method for {@link
	 * com.crawljax.browser.EmbeddedBrowser#isVisible(com.crawljax.core.state.Identification)}.
	 */
	@Test
	public final void testIsVisible() {
		try {
			browser.isVisible(new Identification(How.xpath, "/HTML"));
		} catch (BrowserConnectionException e) {
			// this is how its designed.
			return;
		}
		Assert.fail("A BrowserconnectionException must be thrown");
	}

	/**
	 * Test method for {@link
	 * com.crawljax.browser.EmbeddedBrowser#getInputWithRandomValue(com.crawljax.forms.FormInput)}.
	 */
	@Test
	public final void testGetInputWithRandomValue() {
		try {
			browser.getInputWithRandomValue(
			        new FormInput("text", new Identification(How.xpath, "/HTML"), "abc"));
		} catch (BrowserConnectionException e) {
			// this is how its designed.
			return;
		}
		Assert.fail("A BrowserconnectionException must be thrown");
	}

	/**
	 * Test method for {@link com.crawljax.browser.EmbeddedBrowser#getFrameDom(java.lang.String)}.
	 */
	@Test
	public final void testGetFrameDom() {
		try {
			browser.getFrameDom("123");
		} catch (BrowserConnectionException e) {
			// this is how its designed.
			return;
		}
		Assert.fail("A BrowserconnectionException must be thrown");
	}

	/**
	 * Test method for {@link
	 * com.crawljax.browser.EmbeddedBrowser#elementExists(com.crawljax.core.state.Identification)}.
	 */
	@Test
	public final void testElementExists() {
		try {
			browser.elementExists(new Identification(How.xpath, "/HTML"));
		} catch (BrowserConnectionException e) {
			// this is how its designed.
			return;
		}
		Assert.fail("A BrowserconnectionException must be thrown");
	}

	/**
	 * Test method for {@link
	 * com.crawljax.browser.EmbeddedBrowser#getWebElement(com.crawljax.core.state.Identification)}.
	 */
	@Test
	public final void testGetWebElement() {
		try {
			browser.getWebElement(new Identification(How.xpath, "/HTML"));
		} catch (BrowserConnectionException e) {
			// this is how its designed.
			return;
		}
		Assert.fail("A BrowserconnectionException must be thrown");
	}

	/**
	 * Test method for {@link com.crawljax.browser.EmbeddedBrowser#saveScreenShot(java.io.File)}.
	 *
	 * @throws CrawljaxException
	 *             when screenshotting failed
	 */
	@Test
	public final void testSaveScreenShot() throws CrawljaxException {
		try {
			browser.saveScreenShot(new File("/tmp/file"));
		} catch (BrowserConnectionException e) {
			// this is how its designed.
			return;
		}
		Assert.fail("A BrowserconnectionException must be thrown");
	}
}
