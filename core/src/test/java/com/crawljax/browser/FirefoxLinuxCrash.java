// Copyright 2010 Google Inc. All Rights Reserved.

package com.crawljax.browser;

import java.io.IOException;

import org.junit.Assume;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.crawljax.test.BrowserTest;

/**
 * This is the base class for WebDriver based Firefox 'Crash' Tests running on Linux only. It's a
 * Linux only implementation because of its 'killall' statement. Caution: This test based on this
 * class will most-likely KILL all your running firefox instances.
 */
@Category(BrowserTest.class)
public abstract class FirefoxLinuxCrash {

	private static final int DEFAULT_SLEEP_TIMEOUT = 1000;
	private WebDriver driver;

	private boolean onPosix() {
		Platform current = Platform.getCurrent();
		switch (current) {
			case LINUX:
			case MAC:
			case UNIX:
				return true;
			case ANY:
			case VISTA:
			case WINDOWS:
			case XP:
			case ANDROID:
			default:
				return false;
		}
	}

	/**
	 * Start a browser, than kill it hard!
	 * 
	 * @throws InterruptedException
	 *             when the Thread.sleep can not be executed.
	 */
	@Before
	public void setUp() throws InterruptedException {
		Assume.assumeTrue(onPosix());

		try {
			driver = new FirefoxDriver();
		} catch (WebDriverException e) {
			Assume.assumeNoException(e);
		}

		Assume.assumeNotNull(driver);

		Thread.sleep(DEFAULT_SLEEP_TIMEOUT);

		try {
			Runtime.getRuntime().exec("/usr/bin/killall firefox-bin --verbose");
		} catch (IOException e) {
			Assume.assumeNoException(e);
		}

		Thread.sleep(DEFAULT_SLEEP_TIMEOUT);
	}

	/**
	 * @return the 'crashed'-driver
	 */
	public WebDriver getCrashedDriver() {
		return driver;
	}

}