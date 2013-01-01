package com.crawljax.core.configuration;

/**
 * This class accepts all frames.
 * 
 * @author Stefan Lenselink <slenselink@google.com>
 * @version $Id$
 */
public class AcceptAllFramesChecker implements IgnoreFrameChecker {
	@Override
	public boolean isFrameIgnored(String frameId) {
		return false;
	}
}
