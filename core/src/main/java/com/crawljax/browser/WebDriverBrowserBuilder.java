package com.crawljax.browser;

import javax.inject.Inject;
import javax.inject.Provider;

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

import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.ProxyConfiguration.ProxyType;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSortedSet;

/**
 * Default implementation of the EmbeddedBrowserBuilder based on Selenium WebDriver API.
 */
public class WebDriverBrowserBuilder implements Provider<EmbeddedBrowser> {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebDriverBrowserBuilder.class);
	private final CrawljaxConfiguration configuration;

	@Inject
	public WebDriverBrowserBuilder(CrawljaxConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Build a new WebDriver based EmbeddedBrowser.
	 * 
	 * @see EmbeddedBrowserBuilder#buildEmbeddedBrowser(CrawljaxConfigurationReader)
	 * @param configuration
	 *            the configuration object to read the config values from
	 * @return the new build WebDriver based embeddedBrowser
	 */
	@Override
	public EmbeddedBrowser get() {
		LOGGER.debug("Setting up a Browser");
		// Retrieve the config values used
		ImmutableSortedSet<String> filterAttributes =
		        configuration.getCrawlRules().getPreCrawlConfig().getFilterAttributeNames();
		long crawlWaitReload = configuration.getCrawlRules().getWaitAfterReloadUrl();
		long crawlWaitEvent = configuration.getCrawlRules().getWaitAfterEvent();

		// Determine the requested browser type
		EmbeddedBrowser browser = null;
		switch (configuration.getBrowserConfig().getBrowsertype()) {
			case firefox:
				browser = newFireFoxBrowser(filterAttributes, crawlWaitReload, crawlWaitEvent);
				break;
			case ie:
				browser = WebDriverBackedEmbeddedBrowser.withDriver(new InternetExplorerDriver(),
				        filterAttributes, crawlWaitEvent, crawlWaitReload);
				break;
			case chrome:
				browser = newChromeBrowser(filterAttributes, crawlWaitReload, crawlWaitEvent);
				break;

			case remote:
				browser = WebDriverBackedEmbeddedBrowser.withRemoteDriver(configuration
				        .getBrowserConfig().getRemoteHubUrl(), filterAttributes, crawlWaitEvent,
				        crawlWaitReload);
				break;
			case htmlunit:
				browser = newHtmlUnitBrowser(filterAttributes, crawlWaitReload, crawlWaitEvent);
				break;
			case android:
				browser = WebDriverBackedEmbeddedBrowser.withDriver(new AndroidDriver(),
				        filterAttributes, crawlWaitEvent, crawlWaitReload);
				break;

			case iphone:
				try {
					browser = WebDriverBackedEmbeddedBrowser.withDriver(new IPhoneDriver(),
					        filterAttributes, crawlWaitEvent, crawlWaitReload);
				} catch (Exception e) {
					LOGGER.error("Could not load driver: " + e.getMessage(), e);
					throw new CrawljaxException(e);
				}

			default:
				browser = WebDriverBackedEmbeddedBrowser.withDriver(new FirefoxDriver(),
				        filterAttributes, crawlWaitEvent, crawlWaitReload);
		}
		configuration.getPlugins().runOnBrowserCreatedPlugins(browser);
		return browser;
	}

	private EmbeddedBrowser newHtmlUnitBrowser(ImmutableSortedSet<String> filterAttributes,
	        long crawlWaitReload, long crawlWaitEvent) {
		HtmlUnitDriver driverHtmlUnit = new HtmlUnitDriver(BrowserVersion.FIREFOX_17);
		driverHtmlUnit.setJavascriptEnabled(true);
		if (configuration.getProxyConfiguration() != null) {
			driverHtmlUnit.setProxy(configuration.getProxyConfiguration().getHostname(),
			        configuration.getProxyConfiguration().getPort());
		}

		return WebDriverBackedEmbeddedBrowser.withDriver(driverHtmlUnit,
		        filterAttributes, crawlWaitEvent, crawlWaitReload);
	}

	private EmbeddedBrowser newFireFoxBrowser(ImmutableSortedSet<String> filterAttributes,
	        long crawlWaitReload, long crawlWaitEvent) {
		if (configuration.getProxyConfiguration() != null) {
			FirefoxProfile profile = new FirefoxProfile();
			String lang = configuration.getBrowserConfig().getLangOrNull();
			if (!Strings.isNullOrEmpty(lang)) {
				profile.setPreference("intl.accept_languages", lang);
			}

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
	}

	private EmbeddedBrowser newChromeBrowser(ImmutableSortedSet<String> filterAttributes,
	        long crawlWaitReload, long crawlWaitEvent) {
		ChromeDriver driverChrome;
		if (configuration.getProxyConfiguration() != null
		        && configuration.getProxyConfiguration().getType() != ProxyType.NOTHING) {
			ChromeOptions optionsChrome = new ChromeOptions();
			String lang = configuration.getBrowserConfig().getLangOrNull();
			if (!Strings.isNullOrEmpty(lang)) {
				optionsChrome.setExperimentalOptions("intl.accept_languages", lang);
			}
			optionsChrome.addArguments("--proxy-server=http://"
			        + configuration.getProxyConfiguration().getHostname() + ":"
			        + configuration.getProxyConfiguration().getPort());
			driverChrome = new ChromeDriver(optionsChrome);
		} else {
			driverChrome = new ChromeDriver();
		}

		return WebDriverBackedEmbeddedBrowser.withDriver(driverChrome, filterAttributes,
		        crawlWaitEvent, crawlWaitReload);
	}
}
