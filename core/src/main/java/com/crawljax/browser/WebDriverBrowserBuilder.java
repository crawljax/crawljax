package com.crawljax.browser;

import javax.inject.Inject;
import javax.inject.Provider;

import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.ProxyConfiguration;
import com.crawljax.core.configuration.ProxyConfiguration.ProxyType;
import com.crawljax.core.plugin.Plugins;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSortedSet;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the EmbeddedBrowserBuilder based on Selenium WebDriver API.
 */
public class WebDriverBrowserBuilder implements Provider<EmbeddedBrowser> {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebDriverBrowserBuilder.class);
	private final CrawljaxConfiguration configuration;
	private final Plugins plugins;

	@Inject
	public WebDriverBrowserBuilder(CrawljaxConfiguration configuration, Plugins plugins) {
		this.configuration = configuration;
		this.plugins = plugins;
	}

	/**
	 * Build a new WebDriver based EmbeddedBrowser.
	 * 
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
		EmbeddedBrowser.BrowserType browserType = configuration.getBrowserConfig().getBrowsertype();
		try {
			switch (browserType) {
				case FIREFOX:
					browser =
					        newFireFoxBrowser(filterAttributes, crawlWaitReload, crawlWaitEvent);
					break;
				case INTERNET_EXPLORER:
					browser =
					        WebDriverBackedEmbeddedBrowser.withDriver(
					                new InternetExplorerDriver(),
					                filterAttributes, crawlWaitEvent, crawlWaitReload);
					break;
				case CHROME:
					browser = newChromeBrowser(filterAttributes, crawlWaitReload, crawlWaitEvent);
					break;
				case REMOTE:
					browser =
					        WebDriverBackedEmbeddedBrowser.withRemoteDriver(configuration
					                .getBrowserConfig().getRemoteHubUrl(), filterAttributes,
					                crawlWaitEvent, crawlWaitReload);
					break;
				case PHANTOMJS:
					browser =
					        newPhantomJSDriver(filterAttributes, crawlWaitReload, crawlWaitEvent);
					break;
				default:
					throw new IllegalStateException("Unrecognized browsertype "
					        + configuration.getBrowserConfig().getBrowsertype());
			}
		} catch (IllegalStateException e) {
			LOGGER.error("Crawling with {} failed: " + e.getMessage(), browserType.toString());
			throw e;
		}
		plugins.runOnBrowserCreatedPlugins(browser);
		return browser;
	}

	private EmbeddedBrowser newFireFoxBrowser(ImmutableSortedSet<String> filterAttributes,
	        long crawlWaitReload, long crawlWaitEvent) {
		if (configuration.getProxyConfiguration() != null) {
			FirefoxProfile profile = new FirefoxProfile();
			String lang = configuration.getBrowserConfig().getLangOrNull();
			if (!Strings.isNullOrEmpty(lang)) {
				profile.setPreference("intl.accept_languages", lang);
			}

			profile.setPreference("network.proxy.http", configuration.getProxyConfiguration()
			        .getHostname());
			profile.setPreference("network.proxy.http_port", configuration
			        .getProxyConfiguration().getPort());
			profile.setPreference("network.proxy.type", configuration.getProxyConfiguration()
			        .getType().toInt());
			/* use proxy for everything, including localhost */
			profile.setPreference("network.proxy.no_proxies_on", "");

			return WebDriverBackedEmbeddedBrowser.withDriver(new FirefoxDriver(profile),
			        filterAttributes, crawlWaitReload, crawlWaitEvent);
		}

		return WebDriverBackedEmbeddedBrowser.withDriver(new FirefoxDriver(), filterAttributes,
		        crawlWaitEvent, crawlWaitReload);
	}

	private EmbeddedBrowser newChromeBrowser(ImmutableSortedSet<String> filterAttributes,
	        long crawlWaitReload, long crawlWaitEvent) {
		ChromeDriver driverChrome;
		if (configuration.getProxyConfiguration() != null
		        && configuration.getProxyConfiguration().getType() != ProxyType.NOTHING) {
			ChromeOptions optionsChrome = new ChromeOptions();
			String lang = configuration.getBrowserConfig().getLangOrNull();
			if (!Strings.isNullOrEmpty(lang)) {
				optionsChrome.addArguments("--lang=" + lang);
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

	private EmbeddedBrowser newPhantomJSDriver(ImmutableSortedSet<String> filterAttributes,
	        long crawlWaitReload, long crawlWaitEvent) {

		DesiredCapabilities caps = new DesiredCapabilities();
		caps.setCapability("takesScreenshot", true);
		caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, new String[]{"--webdriver-loglevel=WARN"});
		final ProxyConfiguration proxyConf = configuration
				.getProxyConfiguration();
		if (proxyConf != null && proxyConf.getType() != ProxyType.NOTHING) {
			final String proxyAddrCap = "--proxy=" + proxyConf.getHostname()
					+ ":" + proxyConf.getPort();
			final String proxyTypeCap = "--proxy-type=http";
			final String[] args = new String[] { proxyAddrCap, proxyTypeCap };
			caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, args);
		}
		
		PhantomJSDriver phantomJsDriver = new PhantomJSDriver(caps);

		return WebDriverBackedEmbeddedBrowser.withDriver(phantomJsDriver, filterAttributes,
		        crawlWaitEvent, crawlWaitReload);
	}

}
