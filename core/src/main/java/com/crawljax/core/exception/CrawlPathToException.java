// Copyright 2010 Google Inc. All Rights Reserved.

package com.crawljax.core.exception;

import com.crawljax.core.state.CrawlPath;

/**
 * This exception wraps a CrawlPath into a Throwable. Basically providing a StackTrace of the Path
 * taken to reach an Exception point.
 * 
 * @author slenselink@google.com (Stefan Lenselink)
 */
public class CrawlPathToException extends Exception {

	/**
	 * Generated serial version UID
	 */
	private static final long serialVersionUID = 5794807783433728198L;

	/**
	 * Build a new {@link CrawlPathToException} given a path taken.
	 * 
	 * @param message
	 *            the message to supply with this exception
	 * @param path
	 *            the CrawlPath taken that causes this exception.
	 * @param cause
	 *            the root cause of this exception.
	 */
	public CrawlPathToException(String message, CrawlPath path, Throwable cause) {
		super(message, cause);
		setStackTrace(path.asStackTrace());
	}

	/**
	 * Build a new {@link CrawlPathToException} given a path taken.
	 * 
	 * @param message
	 *            the message to supply with this exception
	 * @param path
	 *            the CrawlPath taken that causes this exception.
	 */
	public CrawlPathToException(String message, CrawlPath path) {
		this(message, path, null);
	}

	/**
	 * Build a new {@link CrawlPathToException} given a path taken.
	 * 
	 * @param path
	 *            the CrawlPath taken that causes this exception.
	 * @param cause
	 *            the root cause of this exception.
	 */
	public CrawlPathToException(CrawlPath path, Throwable cause) {
		this(null, path, cause);
	}

	/**
	 * Build a new {@link CrawlPathToException} given a path taken.
	 * 
	 * @param path
	 *            the CrawlPath taken that causes this exception.
	 */
	public CrawlPathToException(CrawlPath path) {
		this(null, path, null);
	}
}
