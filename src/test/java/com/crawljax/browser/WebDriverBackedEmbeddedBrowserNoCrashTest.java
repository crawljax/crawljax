// Copyright 2010 Google Inc. All Rights Reserved.

package com.crawljax.browser;

import static org.junit.Assert.fail;
import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.crawljax.core.CrawljaxException;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.core.state.Identification.How;
import com.crawljax.forms.FormInput;

/**
 * This Test checks the 'default' behavior of {@link EmbeddedBrowser} implemented by
 * {@link WebDriverBackedEmbeddedBrowser} on invalid input while the used browser is still active.
 * 
 * @version $Id: WebDriverBackedEmbeddedBrowserNoCrashTest.java 446 2010-09-16 09:17:24Z
 *          slenselink@google.com $
 * @author slenselink@google.com (Stefan Lenselink)
 */
public class WebDriverBackedEmbeddedBrowserNoCrashTest {

	private EmbeddedBrowser browser;

	/**
	 * Make a new Browser for every test.
	 */
	@Before
	public void setUp() {
		browser = WebDriverBackedEmbeddedBrowser.withDriver(new FirefoxDriver());
	}

	/**
	 * Clean-up our stuff.
	 */
	@After
	public void tearDown() {
		try {
			browser.close();
		} catch (NullPointerException e) {
			// This will be thrown when close is already been called. Due to a WebDriver bug.
			return;
		}
	}

	/**
	 * Test method for {@link com.crawljax.browser.EmbeddedBrowser#close()}.
	 */
	@Test
	public final void testClose() {
		browser.close();
	}

	/**
	 * Test method for {@link com.crawljax.browser.EmbeddedBrowser#closeOtherWindows()}.
	 */
	@Test
	public final void testCloseOtherWindows() {
		browser.closeOtherWindows();
	}

	/**
	 * Test method for
	 * {@link com.crawljax.browser.EmbeddedBrowser#executeJavaScript(java.lang.String)}.
	 * 
	 * @throws CrawljaxException
	 *             when the script can not be executed
	 */
	@Test
	@Ignore
	public final void testExecuteJavaScript() throws CrawljaxException {
		try {
			browser.executeJavaScript("alert('testing');");

		} catch (CrawljaxException e) {
			fail("A WebDriverException needed to be thrown");
		}

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
		browser
		        .fireEvent(new Eventable(new Identification(How.xpath, "/RUBISH"),
		                EventType.click));
	}

	/**
	 * Test method for {@link com.crawljax.browser.EmbeddedBrowser#getCurrentUrl()}.
	 */
	@Test
	public final void testGetCurrentUrl() {
		browser.getCurrentUrl();
	}

	/**
	 * Test method for {@link com.crawljax.browser.EmbeddedBrowser#getDom()}.
	 * 
	 * @throws CrawljaxException
	 *             when the dom can not be downloaded.
	 */
	@Test
	public final void testGetDom() throws CrawljaxException {
		browser.goToUrl("http://www.google.nl/");
		browser.getDom();
	}

	/**
	 * Test method for {@link com.crawljax.browser.EmbeddedBrowser#getDomWithoutIframeContent()}.
	 * 
	 * @throws CrawljaxException
	 *             when the the dom can not be downloaded.
	 */
	@Test
	public final void testGetDomWithoutIframeContent() throws CrawljaxException {
		browser.getDomWithoutIframeContent();
	}

	/**
	 * Test method for {@link com.crawljax.browser.EmbeddedBrowser#goBack()}.
	 */
	@Test
	public final void testGoBack() {
		browser.goBack();
	}

	/**
	 * Test method for {@link com.crawljax.browser.EmbeddedBrowser#goToUrl(java.lang.String)}.
	 * 
	 * @throws CrawljaxException
	 *             when the url can not be opend.
	 */
	@Test
	public final void testGoToUrl() throws CrawljaxException {
		// TODO Stefan; bug in WebDriver iff you specify bla:// will end up in NullPointer.
		browser.goToUrl("http://non.exsisting.domain");
	}

	/**
	 * Test method for
	 * {@link com.crawljax.browser.EmbeddedBrowser#input(com.crawljax.core.state.Identification, java.lang.String)}
	 * .
	 * 
	 * @throws CrawljaxException
	 *             when the input can not be found
	 */
	@Test
	public final void testInput() throws CrawljaxException {
		Assert.assertFalse("Wrong Xpath so false because of error", browser.input(
		        new Identification(How.xpath, "/RUBISH"), "some"));
	}

	/**
	 * Test method for
	 * {@link com.crawljax.browser.EmbeddedBrowser#isVisible(com.crawljax.core.state.Identification)}
	 * .
	 */
	@Test
	public final void testIsVisible() {
		Assert.assertFalse("Wrong Xpath so not visible", browser.isVisible(new Identification(
		        How.xpath, "/RUBISH")));
	}

	/**
	 * Test method for
	 * {@link com.crawljax.browser.EmbeddedBrowser#getInputWithRandomValue(com.crawljax.forms.FormInput)}
	 * .
	 */
	@Test
	public final void testGetInputWithRandomValue() {
		Assert.assertNull("Wrong Xpath so null as result of InputWithRandomValue", browser
		        .getInputWithRandomValue(new FormInput("text", new Identification(How.xpath,
		                "/RUBISH"), "abc")));
	}

	/**
	 * Test method for {@link com.crawljax.browser.EmbeddedBrowser#getFrameDom(java.lang.String)}.
	 */
	@Test
	public final void testGetFrameDom() {
		Assert.assertTrue("Wrong FrameID so empty", browser.getFrameDom("123").equals(""));
	}

	/**
	 * Test method for
	 * {@link com.crawljax.browser.EmbeddedBrowser#elementExists(com.crawljax.core.state.Identification)}
	 * .
	 */
	@Test
	public final void testElementExists() {
		Assert.assertFalse("Wrong Xpath so element does not exsist", browser
		        .elementExists(new Identification(How.xpath, "/RUBISH")));
	}

	/**
	 * Test method for
	 * {@link com.crawljax.browser.EmbeddedBrowser#getWebElement(com.crawljax.core.state.Identification)}
	 * .
	 */
	@Test
	public final void testGetWebElement() {
		try {
			browser.getWebElement(new Identification(How.xpath, "/RUBISH"));
		} catch (NoSuchElementException e) {
			// Expected behavior
			return;
		}
		Assert.fail("NoSuchElementException should have been thrown");
	}

}
