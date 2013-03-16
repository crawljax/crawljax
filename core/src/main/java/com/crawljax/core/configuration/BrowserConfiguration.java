package com.crawljax.core.configuration;

import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.Immutable;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.browser.EmbeddedBrowserBuilder;
import com.crawljax.browser.WebDriverBrowserBuilder;
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

	private final BrowserType browsertype;
	private final int numberOfBrowsers;
	private final boolean bootstrap;
	private final EmbeddedBrowserBuilder browserBuilder;
	private String remoteHubUrl;

	/**
	 * @param browsertype
	 *            The browser you'd like to use.
	 * @param numberOfBrowsers
	 *            The number of browsers you'd like to use. They will be started as soon as the
	 *            crawl starts.
	 * @param remoteUrl
	 *            the URL of the remote HUB
	 */
	public static BrowserConfiguration remoteConfig(int numberOfBrowsers, boolean bootstrap,
	        String remoteUrl) {
		BrowserConfiguration config =
		        new BrowserConfiguration(BrowserType.remote, numberOfBrowsers, bootstrap);
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
		this(browsertype, numberOfBrowsers, true);
	}

	/**
	 * @param browsertype
	 *            The browser you'd like to use.
	 * @param numberOfBrowsers
	 *            The number of browsers you'd like to use. They will be started as soon as the
	 *            crawl starts.
	 * @param bootstrap
	 *            if you want the browsers to start when the crawler starts. If <code>false</code>
	 *            the browser will only be started when they are needed.
	 */
	public BrowserConfiguration(BrowserType browsertype, int numberOfBrowsers, boolean bootstrap) {
		this(browsertype, numberOfBrowsers, bootstrap, new WebDriverBrowserBuilder());
	}

	/**
	 * @param browsertype
	 *            The browser you'd like to use.
	 * @param numberOfBrowsers
	 *            The number of browsers you'd like to use. They will be started as soon as the
	 *            crawl starts.
	 * @param bootstrap
	 *            if you want the browsers to start when the crawler starts. If <code>false</code>
	 *            the browser will only be started when they are needed.
	 * @param builder
	 *            a custom {@link WebDriverBrowserBuilder}.
	 */
	public BrowserConfiguration(BrowserType browsertype, int numberOfBrowsers, boolean bootstrap,
	        WebDriverBrowserBuilder builder) {
		Preconditions.checkArgument(numberOfBrowsers > 0,
		        "Number of browsers should be 1 or more");
		Preconditions.checkNotNull(browsertype);
		Preconditions.checkNotNull(builder);

		this.browsertype = browsertype;
		this.numberOfBrowsers = numberOfBrowsers;
		this.bootstrap = bootstrap;
		this.browserBuilder = builder;
	}

	public BrowserType getBrowsertype() {
		return browsertype;
	}

	public int getNumberOfBrowsers() {
		return numberOfBrowsers;
	}

	public boolean isBootstrap() {
		return bootstrap;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (bootstrap ? 1231 : 1237);
		result = prime * result + ((browsertype == null) ? 0 : browsertype.hashCode());
		result = prime * result + numberOfBrowsers;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		BrowserConfiguration other = (BrowserConfiguration) obj;
		if (bootstrap != other.bootstrap) {
			return false;
		}
		if (browsertype != other.browsertype) {
			return false;
		}
		if (numberOfBrowsers != other.numberOfBrowsers) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BrowserConfiguration [browsertype=");
		builder.append(browsertype);
		builder.append(", numberOfBrowsers=");
		builder.append(numberOfBrowsers);
		builder.append(", bootstrap=");
		builder.append(bootstrap);
		builder.append("]");
		return builder.toString();
	}

	public EmbeddedBrowserBuilder getBrowserBuilder() {
		return browserBuilder;
	}

	public String getRemoteHubUrl() {
		return remoteHubUrl;
	}

}
