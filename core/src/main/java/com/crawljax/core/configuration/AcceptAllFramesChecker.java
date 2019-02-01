package com.crawljax.core.configuration;

/**
 * This class accepts all frames.
 *
 * @author Stefan Lenselink &lt;slenselink@google.com&gt;
 */
public class AcceptAllFramesChecker implements IgnoreFrameChecker {
	@Override
	public boolean isFrameIgnored(String frameId) {
		return false;
	}
}
