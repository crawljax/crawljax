package com.crawljax.browser;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openqa.selenium.android.AndroidDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.iphone.IPhoneDriver;

import com.crawljax.core.configuration.CrawljaxConfigurationReader;

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
	public EmbeddedBrowser buildEmbeddedBrowser(CrawljaxConfigurationReader configuration) {
		// Retrieve the config values used
		List<String> filterAttributes = configuration.getFilterAttributeNames();
		int crawlWaitReload = configuration.getCrawlSpecificationReader().getWaitAfterReloadUrl();
		int crawlWaitEvent = configuration.getCrawlSpecificationReader().getWaitAfterEvent();

		// Determine the requested browser type
		switch (configuration.getBrowser()) {
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
				        configuration.getFilterAttributeNames(), configuration
				                .getCrawlSpecificationReader().getWaitAfterEvent(), configuration
				                .getCrawlSpecificationReader().getWaitAfterReloadUrl());

			case ie:
				return WebDriverBackedEmbeddedBrowser.withDriver(new InternetExplorerDriver(),
				        configuration.getFilterAttributeNames(), configuration
				                .getCrawlSpecificationReader().getWaitAfterEvent(), configuration
				                .getCrawlSpecificationReader().getWaitAfterReloadUrl());

			case chrome:
				// Guifre Ruiz: Added proxy config support for Chrome
				ChromeOptions optionsChrome = new ChromeOptions();
				optionsChrome.addArguments("--proxy-server=http://"
				        + configuration.getProxyConfiguration().getHostname() + ":"
				        + configuration.getProxyConfiguration().getPort());
				ChromeDriver driverChrome = new ChromeDriver(optionsChrome);

				return WebDriverBackedEmbeddedBrowser.withDriver(driverChrome, configuration
				        .getFilterAttributeNames(), configuration.getCrawlSpecificationReader()
				        .getWaitAfterEvent(), configuration.getCrawlSpecificationReader()
				        .getWaitAfterReloadUrl());

			case remote:
				return WebDriverBackedEmbeddedBrowser.withRemoteDriver(
				        configuration.getRemoteHubUrl(), configuration.getFilterAttributeNames(),
				        configuration.getCrawlSpecificationReader().getWaitAfterEvent(),
				        configuration.getCrawlSpecificationReader().getWaitAfterReloadUrl());

			case htmlunit:
				HtmlUnitDriver driverHtmlUnit = new HtmlUnitDriver(true);
				driverHtmlUnit.setProxy(configuration.getProxyConfiguration().getHostname(),
				        configuration.getProxyConfiguration().getPort());

				return WebDriverBackedEmbeddedBrowser.withDriver(driverHtmlUnit, configuration
				        .getFilterAttributeNames(), configuration.getCrawlSpecificationReader()
				        .getWaitAfterEvent(), configuration.getCrawlSpecificationReader()
				        .getWaitAfterReloadUrl());

			case iphone:
				try {
					return WebDriverBackedEmbeddedBrowser.withDriver(new IPhoneDriver(),
					        configuration.getFilterAttributeNames(), configuration
					                .getCrawlSpecificationReader().getWaitAfterEvent(),
					        configuration.getCrawlSpecificationReader().getWaitAfterReloadUrl());
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
				}

			case android:
				return WebDriverBackedEmbeddedBrowser.withDriver(new AndroidDriver(),
				        configuration.getFilterAttributeNames(), configuration
				                .getCrawlSpecificationReader().getWaitAfterEvent(), configuration
				                .getCrawlSpecificationReader().getWaitAfterReloadUrl());

			default:
				return WebDriverBackedEmbeddedBrowser.withDriver(new FirefoxDriver(),
				        configuration.getFilterAttributeNames(), configuration
				                .getCrawlSpecificationReader().getWaitAfterEvent(), configuration
				                .getCrawlSpecificationReader().getWaitAfterReloadUrl());
		}
	}
}
