package com.crawljax.plugins.testcasegenerator;

import com.crawljax.core.configuration.BrowserConfiguration;

public class TestConfiguration {

	public enum StateEquivalenceAssertionMode {
		DOM, VISUAL, BOTH, HYBRID;

		@Override
		public String toString() {
			return this.getClass().getSimpleName() + "." + this.name();
		}
	}

	private StateEquivalenceAssertionMode assertionMode;
	private BrowserConfiguration browserConfig;

	public TestConfiguration(BrowserConfiguration browserConfig) {
		this.setAssertionMode(StateEquivalenceAssertionMode.BOTH);
		this.setBrowserConfig(browserConfig);
	}

	public TestConfiguration(StateEquivalenceAssertionMode assertionMode) {
		this.browserConfig = null;
		this.assertionMode = assertionMode;
	}

	public TestConfiguration(StateEquivalenceAssertionMode assertionMode,
	        BrowserConfiguration browserConfiguration) {
		this.browserConfig = browserConfiguration;
		this.assertionMode = assertionMode;
	}

	public BrowserConfiguration getBrowserConfig() {
		return browserConfig;
	}

	public void setBrowserConfig(BrowserConfiguration browserConfig) {
		this.browserConfig = browserConfig;
	}

	public StateEquivalenceAssertionMode getAssertionMode() {
		return assertionMode;
	}

	public void setAssertionMode(StateEquivalenceAssertionMode assertionMode) {
		this.assertionMode = assertionMode;
	}
}
