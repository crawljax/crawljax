package com.crawljax.browser;

import java.io.File;

import org.openqa.selenium.By;

import com.crawljax.core.CrawljaxException;
import com.crawljax.core.state.Eventable;

/**
 * Real Empty class for place holding {@link EmbeddedBrowser} in UnitTests. There is absolutely NO
 * contents in this class other than the required methods which performs NOTHING! (Returning false
 * in boolean case, null otherwise)
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id$
 */
public class DummyBrowser implements EmbeddedBrowser {

	@Override
	public boolean canGoBack() {
		return false;
	}

	@Override
	public void close() {
	}

	@Override
	public void closeOtherWindows() {
	}

	@Override
	public Object executeJavaScript(String script) throws CrawljaxException {
		return null;
	}

	@Override
	public boolean fireEvent(Eventable event) throws CrawljaxException {
		return false;
	}

	@Override
	public String getCurrentUrl() {
		return null;
	}

	@Override
	public String getDom() throws CrawljaxException {
		return null;
	}

	@Override
	public String getDomWithoutIframeContent() throws CrawljaxException {
		return null;
	}

	@Override
	public void goBack() {
	}

	@Override
	public void goToUrl(String url) throws CrawljaxException {
	}

	@Override
	public boolean input(Eventable eventable, String text) throws CrawljaxException {
		return false;
	}

	@Override
	public boolean isVisible(By locater) {
		return false;
	}

	@Override
	public void saveScreenShot(File pngFile) {
	}

	@Override
	public EmbeddedBrowser clone() {
		return new DummyBrowser();
	}
}
