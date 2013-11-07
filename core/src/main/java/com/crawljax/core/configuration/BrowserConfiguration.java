package com.crawljax.core.configuration;

import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.Immutable;
import javax.inject.Provider;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.browser.WebDriverBrowserBuilder;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

@Immutable
public class BrowserConfiguration {

	/**
	 * The total number of retries when a browser can not be created.
	 */
	public static final int BROWSER_START_RETRIES = 2;

	/**
	 * The number of milliseconds to sleep when a browser can not be created.
	 */
	public static final long BROWSER_SLEEP_FAILURE = TimeUnit.SECONDS.toMillis(10);

	private static final Provider<EmbeddedBrowser> DEFAULT_BROWSER_BUILDER =
	        new Provider<EmbeddedBrowser>() {

		        @Override
		        public EmbeddedBrowser get() {
			        throw new IllegalStateException(
			                "This is just a placeholder and should not be called");
		        }

		        @Override
		        public String toString() {
			        return "Default webdriver factory";
		        };

	        };

	private final BrowserType browsertype;
	private final int numberOfBrowsers;
	private final Provider<EmbeddedBrowser> browserBuilder;
	private String remoteHubUrl;
	private String lang;

	/**
	 * @param numberOfBrowsers
	 *            The number of browsers you'd like to use. They will be started as soon as the
	 *            crawl starts.
	 * @param remoteUrl
	 *            the URL of the remote HUB
	 */
	public static BrowserConfiguration remoteConfig(int numberOfBrowsers, String remoteUrl) {
		BrowserConfiguration config =
		        new BrowserConfiguration(BrowserType.REMOTE, numberOfBrowsers);
		config.remoteHubUrl = remoteUrl;
		return config;
	}

	/**
	 * This configuration will start one browser of the selected type.
	 * 
	 * @param browsertype
	 *            The browser you would like to run.
	 */
	public BrowserConfiguration(BrowserType browsertype) {
		this(browsertype, 1);
	}

	/**
	 * @param browsertype
	 *            The browser you'd like to use.
	 * @param numberOfBrowsers
	 *            The number of browsers you'd like to use. They will be started as soon as the
	 *            crawl starts.
	 */
	public BrowserConfiguration(BrowserType browsertype, int numberOfBrowsers) {
		this(browsertype, numberOfBrowsers, DEFAULT_BROWSER_BUILDER);
	}

	/**
	 * @param browsertype
	 *            The browser you'd like to use.
	 * @param numberOfBrowsers
	 *            The number of browsers you'd like to use. They will be started as soon as the
	 *            crawl starts.
	 * @param builder
	 *            a custom {@link WebDriverBrowserBuilder}.
	 */
	public BrowserConfiguration(BrowserType browsertype, int numberOfBrowsers,
	        Provider<EmbeddedBrowser> builder) {
		Preconditions.checkArgument(numberOfBrowsers > 0,
		        "Number of browsers should be 1 or more");
		Preconditions.checkNotNull(browsertype);
		Preconditions.checkNotNull(builder);

		this.browsertype = browsertype;
		this.numberOfBrowsers = numberOfBrowsers;
		this.browserBuilder = builder;
	}

	public BrowserType getBrowsertype() {
		return browsertype;
	}

	public int getNumberOfBrowsers() {
		return numberOfBrowsers;
	}

	public Provider<EmbeddedBrowser> getBrowserBuilder() {
		return browserBuilder;
	}

	public String getRemoteHubUrl() {
		return remoteHubUrl;
	}

	public boolean isDefaultBuilder() {
		return browserBuilder.equals(DEFAULT_BROWSER_BUILDER);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
		        .add("browsertype", browsertype)
		        .add("numberOfBrowsers", numberOfBrowsers)
		        .add("browserBuilder", browserBuilder)
		        .add("remoteHubUrl", remoteHubUrl)
		        .add("language", lang)
		        .toString();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(browsertype, numberOfBrowsers, browserBuilder,
		        remoteHubUrl, lang);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof BrowserConfiguration) {
			BrowserConfiguration that = (BrowserConfiguration) object;
			return Objects.equal(this.browsertype, that.browsertype)
			        && Objects.equal(this.numberOfBrowsers, that.numberOfBrowsers)
			        && Objects.equal(this.browserBuilder, that.browserBuilder)
			        && Objects.equal(this.remoteHubUrl, that.remoteHubUrl)
			        && Objects.equal(this.lang, that.lang);
		}
		return false;
	}

	/**
	 * @return the language header setting or <code>null</code> if not set.
	 */
	public String getLangOrNull() {
		return lang;
	}

	/**
	 * @param lang
	 *            the language header in http requests
	 */
	public void setLang(String lang) {
		Preconditions.checkNotNull(lang, "The language cannot be null");
		this.lang = lang;
	}

}
