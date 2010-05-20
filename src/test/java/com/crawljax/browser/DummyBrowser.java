package com.crawljax.browser;

import java.io.File;

import javax.transaction.NotSupportedException;

import org.openqa.selenium.WebElement;

import com.crawljax.core.CrawljaxException;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification;
import com.crawljax.forms.FormInput;

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
	public boolean input(Identification eventable, String text) throws CrawljaxException {
		return false;
	}

	@Override
	public boolean isVisible(Identification locater) {
		return false;
	}

	@Override
	public FormInput getInputWithRandomValue(FormInput inputForm) {
		return null;
	}

	@Override
	public String getFrameDom(String iframeIdentification) {
		return null;
	}

	@Override
	public boolean elementExists(Identification identification) {
		return false;
	}

	@Override
	public WebElement getWebElement(Identification identification) {
		return null;
	}

	@Override
	public void saveScreenShot(File file) throws NotSupportedException {

	}
}
