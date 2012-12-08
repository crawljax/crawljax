// Copyright 2010 Google Inc. All Rights Reserved.

package com.crawljax.browser;

import static org.hamcrest.core.IsInstanceOf.any;

import java.net.ConnectException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

/**
 * Test to see if the WebDriver team consistently throws the same exception when the Host process
 * dies. This test runs only on linux, its not executed by the test-suite as its platform and
 * browser specific.
 */
public class WebDriverFirefoxLinuxCrash extends FirefoxLinuxCrash {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	/**
	 * Test method for {@link WebDriver#close()}.
	 */
	@Test
	public final void testClose() {
		getCrashedDriver().close();
		expectWebDriverExceptionWithConnectionExceptionCause();
	}

	private void expectWebDriverExceptionWithConnectionExceptionCause() {
		exception.expect(WebDriverException.class);
		exception.expectCause(any(ConnectException.class));
	}

	/**
	 * Test method for {@link WebDriver#findElement(org.openqa.selenium.By)}.
	 */
	@Test
	public final void testFindElement() {
		getCrashedDriver().findElement(By.name("q"));
	}

	/**
	 * Test method for {@link WebDriver#findElements(org.openqa.selenium.By)}.
	 */
	@Test
	public final void testFindElements() {
		getCrashedDriver().findElements(By.name("q"));
		expectWebDriverExceptionWithConnectionExceptionCause();
	}

	/**
	 * Test method for {@link WebDriver#get(java.lang.String)}.
	 */
	@Test
	public final void testGet() {
		getCrashedDriver().get("http://www.google.com");
		expectWebDriverExceptionWithConnectionExceptionCause();
	}

	/**
	 * Test method for {@link WebDriver#getCurrentUrl()}.
	 */
	@Test
	public final void testGetCurrentUrl() {
		getCrashedDriver().getCurrentUrl();
		expectWebDriverExceptionWithConnectionExceptionCause();
	}

	/**
	 * Test method for {@link WebDriver#getPageSource()}.
	 */
	@Test
	public final void testGetPageSource() {
		getCrashedDriver().getPageSource();
		expectWebDriverExceptionWithConnectionExceptionCause();
	}

	/**
	 * Test method for {@link WebDriver#getTitle()}.
	 */
	@Test
	public final void testGetTitle() {
		getCrashedDriver().getTitle();
		expectWebDriverExceptionWithConnectionExceptionCause();
	}

	/**
	 * Test method for {@link WebDriver#getWindowHandle()}.
	 */
	@Test
	public final void testGetWindowHandle() {
		getCrashedDriver().getWindowHandle();
		expectWebDriverExceptionWithConnectionExceptionCause();
	}

	/**
	 * Test method for {@link WebDriver#getWindowHandles()}.
	 */
	@Test
	public final void testGetWindowHandles() {
		getCrashedDriver().getWindowHandles();
		expectWebDriverExceptionWithConnectionExceptionCause();
	}

	/**
	 * Test method for {@link WebDriver#manage()}.
	 */
	@Test
	public final void testManage() {
		getCrashedDriver().manage();
		expectWebDriverExceptionWithConnectionExceptionCause();
	}

	/**
	 * Test method for {@link WebDriver#navigate()}.
	 */
	@Test
	public final void testNavigate() {
		getCrashedDriver().navigate();
		expectWebDriverExceptionWithConnectionExceptionCause();
	}

	/**
	 * Test method for {@link WebDriver#quit()}.
	 */
	@Test
	public final void testQuit() {
		getCrashedDriver().quit();
		expectWebDriverExceptionWithConnectionExceptionCause();
	}

	/**
	 * Test method for {@link WebDriver#switchTo()}.
	 */
	@Test
	public final void testSwitchTo() {
		getCrashedDriver().switchTo();
		expectWebDriverExceptionWithConnectionExceptionCause();
	}

}
