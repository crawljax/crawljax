// Copyright 2010 Google Inc. All Rights Reserved.

package com.crawljax.browser;

import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.IOException;

/**
 * This is the base class for WebDriver based Firefox 'Crash' Tests running on Linux only. It's a
 * Linux only implementation because of its 'killall' statement. Caution: This test based on this
 * class will most-likely KILL all your running firefox instances.
 *
 * @version $Id$
 * @author slenselink@google.com (Stefan Lenselink)
 */
public abstract class FirefoxLinuxCrash {

	private static final int DEFAULT_SLEEP_TIMEOUT = 500;
	private WebDriver driver;

	/**
     * Start a browser, than kill it hard!
     *
     * @throws IOException
     *             when the killall command can not be executed
     * @throws InterruptedException
     *             when the Thread.sleep can not be executed.
     */
    @Before
    public void setUp() throws IOException, InterruptedException {
    	driver = new FirefoxDriver();
    	Thread.sleep(DEFAULT_SLEEP_TIMEOUT);
    	Runtime.getRuntime().exec("killall firefox-bin --verbose");
    	Thread.sleep(DEFAULT_SLEEP_TIMEOUT);
    }

    /**
	 * @return the 'crashed'-driver
	 */
	public WebDriver getCrashedDriver() {
		return driver;
	}
    
}