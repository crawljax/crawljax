// Copyright 2010 Google Inc. All Rights Reserved.

package com.crawljax.core.exception;

import org.openqa.selenium.WebDriverException;

/**
 * This {@link RuntimeException} is thrown when a EmbeddedBrowser lost connection to its underlying
 * implementation and so crashed.
 * 
 * @author slenselink@google.com (Stefan Lenselink)
 */
public class BrowserConnectionException extends RuntimeException {

	/**
	 * Generated serial version UID
	 */
	private static final long serialVersionUID = -5149214539340150056L;

	/**
	 * Create a new BrowserConnectionException based on a previous catched RuntimeException.
	 * 
	 * @param exception
	 *            the original exception to wrap.
	 */
	public BrowserConnectionException(WebDriverException exception) {
		super(exception);
	}
}