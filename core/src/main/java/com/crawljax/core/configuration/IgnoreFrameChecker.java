// Copyright 2010 Google Inc. All Rights Reserved.

package com.crawljax.core.configuration;

/**
 * This interface is used to reflect to operation to see if a given frame must be ignored.
 * 
 * @author Stefan Lenselink <slenselink@google.com>
 */
public interface IgnoreFrameChecker {

	/**
	 * Must a given frame identifier be ignored?
	 * 
	 * @param frameId
	 *            the frame identifier
	 * @return true if the specified frame identifier must be ignored
	 */
	boolean isFrameIgnored(String frameId);
}