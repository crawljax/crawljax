// Copyright 2010 Google Inc. All Rights Reserved.

package com.crawljax.browser;

import junit.framework.Assert;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import java.net.ConnectException;

/**
 * Test to see if the WebDriver team consistently throws the same exception when the Host process
 * dies. This test runs only on linux, its not the most elegant way of executing UnitTesting, its
 * not executed by the test-suite as its platform and browser specific.
 *
 * @version $Id$
 * @author slenselink@google.com (Stefan Lenselink)
 */
public class WebDriverFirefoxLinuxCrash extends FirefoxLinuxCrash {
	/**
	 * Test method for {@link WebDriver#close()}.
	 */
	@Test
	public final void testClose() {
		try {
			getCrashedDriver().close();
		} catch (WebDriverException e) {
			Assert.assertEquals(e.getCause().getClass(), ConnectException.class);
		}
	}

	/**
	 * Test method for {@link WebDriver#findElement(org.openqa.selenium.By)}.
	 */
	@Test
	public final void testFindElement() {
		try {
			getCrashedDriver().findElement(By.name("q"));
		} catch (WebDriverException e) {
			Assert.assertEquals(e.getCause().getClass(), ConnectException.class);
		}
	}

	/**
	 * Test method for {@link WebDriver#findElements(org.openqa.selenium.By)}.
	 */
	@Test
	public final void testFindElements() {
		try {
			getCrashedDriver().findElements(By.name("q"));
		} catch (WebDriverException e) {
			Assert.assertEquals(e.getCause().getClass(), ConnectException.class);
		}
	}

	/**
	 * Test method for {@link WebDriver#get(java.lang.String)}.
	 */
	@Test
	public final void testGet() {
		try {
			getCrashedDriver().get("http://www.google.com");
		} catch (WebDriverException e) {
			Assert.assertEquals(e.getCause().getClass(), ConnectException.class);
		}
	}

	/**
	 * Test method for {@link WebDriver#getCurrentUrl()}.
	 */
	@Test
	public final void testGetCurrentUrl() {
		try {
			getCrashedDriver().getCurrentUrl();
		} catch (WebDriverException e) {
			Assert.assertEquals(e.getCause().getClass(), ConnectException.class);
		}
	}

	/**
	 * Test method for {@link WebDriver#getPageSource()}.
	 */
	@Test
	public final void testGetPageSource() {
		try {
			getCrashedDriver().getPageSource();
		} catch (WebDriverException e) {
			Assert.assertEquals(e.getCause().getClass(), ConnectException.class);
		}
	}

	/**
	 * Test method for {@link WebDriver#getTitle()}.
	 */
	@Test
	public final void testGetTitle() {
		try {
			getCrashedDriver().getTitle();
		} catch (WebDriverException e) {
			Assert.assertEquals(e.getCause().getClass(), ConnectException.class);
		}
	}

	/**
	 * Test method for {@link WebDriver#getWindowHandle()}.
	 */
	@Test
	public final void testGetWindowHandle() {
		try {
			getCrashedDriver().getWindowHandle();
		} catch (WebDriverException e) {
			Assert.assertEquals(e.getCause().getClass(), ConnectException.class);
		}
	}

	/**
	 * Test method for {@link WebDriver#getWindowHandles()}.
	 */
	@Test
	public final void testGetWindowHandles() {
		try {
			getCrashedDriver().getWindowHandles();
		} catch (WebDriverException e) {
			Assert.assertEquals(e.getCause().getClass(), ConnectException.class);
		}
	}

	/**
	 * Test method for {@link WebDriver#manage()}.
	 */
	@Test
	public final void testManage() {
		try {
			getCrashedDriver().manage();
		} catch (WebDriverException e) {
			Assert.assertEquals(e.getCause().getClass(), ConnectException.class);
		}
	}

	/**
	 * Test method for {@link WebDriver#navigate()}.
	 */
	@Test
	public final void testNavigate() {
		try {
			getCrashedDriver().navigate();
		} catch (WebDriverException e) {
			Assert.assertEquals(e.getCause().getClass(), ConnectException.class);
		}
	}

	/**
	 * Test method for {@link WebDriver#quit()}.
	 */
	@Test
	public final void testQuit() {
		try {
			getCrashedDriver().quit();
		} catch (WebDriverException e) {
			Assert.assertEquals(e.getCause().getClass(), ConnectException.class);
		}
	}

	/**
	 * Test method for {@link WebDriver#switchTo()}.
	 */
	@Test
	public final void testSwitchTo() {
		try {
			getCrashedDriver().switchTo();
		} catch (WebDriverException e) {
			Assert.assertEquals(e.getCause().getClass(), ConnectException.class);
		}
	}

}
