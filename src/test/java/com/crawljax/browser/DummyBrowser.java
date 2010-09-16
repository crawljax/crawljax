package com.crawljax.browser;

import com.crawljax.core.configuration.CrawljaxConfigurationReader;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification;
import com.crawljax.forms.FormInput;

import org.openqa.selenium.WebElement;

import java.io.File;

/**
 * Real Empty class for place holding {@link EmbeddedBrowser} in UnitTests. There is absolutely NO
 * contents in this class other than the required methods which performs NOTHING! (Returning false
 * in boolean case, null otherwise)
 *
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id$
 */
public class DummyBrowser implements EmbeddedBrowser {

	private String currentUrl = null;

	public DummyBrowser() {
	}

	public DummyBrowser(String currentUrl) {
		this.currentUrl = currentUrl;
	}

	@Override
	public void close() {
	}

	@Override
	public void closeOtherWindows() {
	}

	@Override
	public Object executeJavaScript(String script) {
		return null;
	}

	@Override
	public boolean fireEvent(Eventable event) {
		return false;
	}

	@Override
	public String getCurrentUrl() {
		return currentUrl;
	}

	@Override
	public String getDom() {
		return null;
	}

	@Override
	public String getDomWithoutIframeContent() {
		return null;
	}

	@Override
	public void goBack() {
	}

	@Override
	public void goToUrl(String url) {
	}

	@Override
	public boolean input(Identification eventable, String text) {
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
	public void saveScreenShot(File file) {

	}

	@Override
	public void updateConfiguration(CrawljaxConfigurationReader configuration) {

	}
}
