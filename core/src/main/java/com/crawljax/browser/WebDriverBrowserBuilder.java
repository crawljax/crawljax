package com.crawljax.browser;

import org.openqa.selenium.android.AndroidDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.iphone.IPhoneDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.google.common.collect.ImmutableSortedSet;

/**
 * Default implementation of the EmbeddedBrowserBuilder based on Selenium WebDriver API.
 */
public class WebDriverBrowserBuilder implements EmbeddedBrowserBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebDriverBrowserBuilder.class);

	/**
	 * Build a new WebDriver based EmbeddedBrowser.
	 * 
	 * @see EmbeddedBrowserBuilder#buildEmbeddedBrowser(CrawljaxConfigurationReader)
	 * @param configuration
	 *            the configuration object to read the config values from
	 * @return the new build WebDriver based embeddedBrowser
	 */
	@Override
	public EmbeddedBrowser buildEmbeddedBrowser(CrawljaxConfiguration configuration) {
		// Retrieve the config values used
		ImmutableSortedSet<String> filterAttributes =
		        configuration.getCrawlRules().getPreCrawlConfig().getFilterAttributeNames();
		long crawlWaitReload = configuration.getCrawlRules().getWaitAfterReloadUrl();
		long crawlWaitEvent = configuration.getCrawlRules().getWaitAfterEvent();

		// Determine the requested browser type
		switch (configuration.getBrowserConfig().getBrowsertype()) {
			case firefox:
				if (configuration.getProxyConfiguration() != null) {
					FirefoxProfile profile = new FirefoxProfile();

					profile.setPreference("network.proxy.http", configuration
					        .getProxyConfiguration().getHostname());
					profile.setPreference("network.proxy.http_port", configuration
					        .getProxyConfiguration().getPort());
					profile.setPreference("network.proxy.type", configuration
					        .getProxyConfiguration().getType().toInt());
					/* use proxy for everything, including localhost */
					profile.setPreference("network.proxy.no_proxies_on", "");

					return WebDriverBackedEmbeddedBrowser.withDriver(new FirefoxDriver(profile),
					        filterAttributes, crawlWaitReload, crawlWaitEvent);
				}

				return WebDriverBackedEmbeddedBrowser.withDriver(new FirefoxDriver(),
				        filterAttributes, crawlWaitEvent, crawlWaitReload);

			case ie:
				return WebDriverBackedEmbeddedBrowser.withDriver(new InternetExplorerDriver(),
				        filterAttributes, crawlWaitEvent, crawlWaitReload);

			case chrome:
				ChromeDriver driverChrome;
				if (configuration.getProxyConfiguration() != null) {
					ChromeOptions optionsChrome = new ChromeOptions();
					optionsChrome.addArguments("--proxy-server=http://"
					        + configuration.getProxyConfiguration().getHostname() + ":"
					        + configuration.getProxyConfiguration().getPort());
					driverChrome = new ChromeDriver(optionsChrome);
				} else {
					driverChrome = new ChromeDriver();
				}

				return WebDriverBackedEmbeddedBrowser.withDriver(driverChrome, filterAttributes,
				        crawlWaitEvent, crawlWaitReload);

			case remote:
				return WebDriverBackedEmbeddedBrowser.withRemoteDriver(configuration
				        .getBrowserConfig().getRemoteHubUrl(), filterAttributes, crawlWaitEvent,
				        crawlWaitReload);

			case htmlunit:
				HtmlUnitDriver driverHtmlUnit = new HtmlUnitDriver(BrowserVersion.FIREFOX_10);
				driverHtmlUnit.setJavascriptEnabled(true);
				if (configuration.getProxyConfiguration() != null) {
					driverHtmlUnit.setProxy(configuration.getProxyConfiguration().getHostname(),
					        configuration.getProxyConfiguration().getPort());
				}

				return WebDriverBackedEmbeddedBrowser.withDriver(driverHtmlUnit,
				        filterAttributes, crawlWaitEvent, crawlWaitReload);

			case android:
				return WebDriverBackedEmbeddedBrowser.withDriver(new AndroidDriver(),
				        filterAttributes, crawlWaitEvent, crawlWaitReload);

			case iphone:
				try {
					return WebDriverBackedEmbeddedBrowser.withDriver(new IPhoneDriver(),
					        filterAttributes, crawlWaitEvent, crawlWaitReload);
				} catch (Exception e) {
					LOGGER.error("Could not load driver: " + e.getMessage(), e);
				}

			default:
				return WebDriverBackedEmbeddedBrowser.withDriver(new FirefoxDriver(),
				        filterAttributes, crawlWaitEvent, crawlWaitReload);
		}
	}
}
